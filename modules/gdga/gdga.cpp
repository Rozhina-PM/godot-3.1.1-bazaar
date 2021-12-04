#include "gdga.h"
#include "hmac.h"
#include "uuid.h"
#include "gzip.h"
#include "crash.h"
#include "../../core/project_settings.h"
#include "../../core/os/os.h"
#include "../../core/io/http_client.h"
#include "../../core/os/file_access.h"
#include "../../core/os/dir_access.h"
#include "../../core/list.h"
#include "../../core/variant.h"
#include "../../core/io/json.h"
#include "../../core/dictionary.h"
#include "../../core/string_buffer.h"
#include "../../core/version.h"
#include "../../core/print_string.h"
#include "../../thirdparty/misc/base64.h"

const String DEFAULT_ENDPOINT_ADDR = "GAME_ANALYTICS_ENDPOINT";
const String DEFAULT_GAME_SECRET = "GAME_ANALYTICS_SECRET";
const String DEFAULT_GAME_KEY = "GAME_ANALYTICS_KEY";

Gdga* Gdga::singleton = 0;

Gdga::Gdga()
{
    singleton = this;
    last_server_ts = 0;
    last_server_enabled = false;
    session_num = 1;
    mutex = Mutex::create();
    query_device_info();
    crash_handler(get_setting("gdga/report_crash", false));
}

void Gdga::query_device_info()
{
    device_info.platform = OS::get_singleton()->get_name().to_lower();

    if (device_info.platform == "x11")
        device_info.platform = "linux";

    if (device_info.platform == "android")
    {
        device_info.user_id = OS::get_singleton()->get_unique_id();

        String os_version;
        int ec;
        List<String> args;
        args.push_back("ro.build.version.release");
        OS::get_singleton()->execute("getprop", args, true, 0, &os_version, &ec);

        if (os_version.ends_with("\n"))
            os_version.remove(os_version.size() - 1);

        device_info.os_version = "android " + os_version;
    }
    else
    {
        device_info.user_id = "0";
        device_info.os_version = "linux 0.0.1";
    }

    device_info.sdk_version = "rest api v2";

    device_info.name = OS::get_singleton()->get_model_name();

    device_info.manufacturer = device_info.name;

    //    print("gdga - setup [" + endpoint_addr + "]");
    print("gdga - device_info.name: [" + device_info.name + "]");
    print("gdga - device_info.platform: [" + device_info.platform + "]");
    print("gdga - device_info.os_version: [" + device_info.os_version + "]");
    print("gdga - device_info.user_id: [" + device_info.user_id + "]");
    print("gdga - device_info.manufacturer: [" + device_info.manufacturer + "]");
    print("gdga - device_info.sdk_version: [" + device_info.sdk_version + "]");
}

void Gdga::set_session_num(int num)
{
    session_num = num;
    print("gdga - session_num: " + String::num(session_num) + "]");
}

void Gdga::print(const String& str) const
{
    if (get_setting("gdga/debug_info", false))
        print_line(str);
}

template<typename T>
T Gdga::get_setting(const String& name, const T& def) const
{
    Variant value = GLOBAL_GET(name);

    if (value.get_type() == Variant::NIL)
        return def;

    return T(value);
}

Error Gdga::send(const String& body, const String& endpoint_addr, const String& route, String& response)
{
    String game_secret = get_setting("gdga/game_secret", DEFAULT_GAME_SECRET);
    String game_key = get_setting("gdga/game_key", DEFAULT_GAME_KEY);

    print("gdga - connecting to " + endpoint_addr);

    bool use_ssl = endpoint_addr.begins_with("https");

    HTTPClient http;
    Error err = http.connect_to_host(endpoint_addr, -1, use_ssl);

    if (err != OK)
    {
        print("gdga - connecting to host error: [" + String::num(err) + "]");
        return err;
    }

    while (http.get_status() == HTTPClient::STATUS_CONNECTING || http.get_status() == HTTPClient::STATUS_RESOLVING)
    {
        http.poll();
        OS::get_singleton()->delay_usec(500000);
    }

    if (http.get_status() != HTTPClient::STATUS_CONNECTED)
    {
        print("gdga - connection status error: [" + String::num(http.get_status()) + "]");
        return FAILED;
    }

    Vector<String> headers;
    headers.push_back("Host: " + endpoint_addr);
    headers.push_back("Content-Type: application/json");
    
    if (get_setting("gdga/use_gzip", true))
    {
        PoolVector<uint8_t> gziped = gzip(body);
        headers.push_back("Authorization: " + hmac_sha_256(gziped, game_secret));
        headers.push_back("Content-Encoding: gzip");
        err = http.request_raw(HTTPClient::METHOD_POST, route, headers, gziped);
    }
    else
    {
        headers.push_back("Authorization: " + hmac_sha_256(body, game_secret));
        err = http.request(HTTPClient::METHOD_POST, route, headers, body);
    }

    if (err != OK)
    {
        print("gdga - requesting error: [" + String::num(err) + "]");
        return err;
    }

    while (http.get_status() == HTTPClient::STATUS_REQUESTING)
    {
        http.poll();
        OS::get_singleton()->delay_usec(500000);
    }

    if (http.get_status() != HTTPClient::STATUS_BODY && http.get_status() != HTTPClient::STATUS_CONNECTED)
    {
        print("gdga - fetching body error: [" + String::num(http.get_status()) + "]");
        return FAILED;
    }

    if (http.has_response())
    {
        List<String> res_headers;
        http.get_response_headers(&res_headers);

        PoolByteArray bytes;

        while (http.get_status() == HTTPClient::STATUS_BODY)
        {
            http.poll();
            PoolByteArray chunk = http.read_response_body_chunk();
            if (chunk.size() == 0)
                OS::get_singleton()->delay_usec(100000);
            else
                bytes.append_array(chunk);
        }

        response = bytes.join("");

        if (http.get_response_code() == 401)
        {
            print("gdga - response error: [401]");
            return ERR_UNAUTHORIZED;
        }

        if (http.get_response_code() == 400)
        {
            print("gdga - response error: [400]");
            return ERR_INVALID_DATA;
        }

        if (http.get_response_code() != 200)
        {
            print("gdga - response error: [" + String::num(http.get_response_code()) + "]");
            return FAILED;
        }

        return OK;
    }

    print("gdga - no response");

    return FAILED;
}

Error Gdga::add_event(const Dictionary& event)
{
    MutexLock mutex_lock(mutex);

    Error err;
    FileAccess* file;

    if (!FileAccess::exists(EVENTS_FILE))
        file = FileAccess::open(EVENTS_FILE, FileAccess::WRITE, &err);
    else
        file = FileAccess::open(EVENTS_FILE, FileAccess::READ_WRITE, &err);

    if (err != OK)
    {
        print("gdga - cannot open file for adding events");
        return err;
    }

    Dictionary ann = annotations();
    String line = JSON::print(merge_dict(event, ann)).c_escape();

    file->seek_end();
    file->store_line(line);
    file->close();

    return file->get_error();
}

void Gdga::add_events(const List<Dictionary>& events)
{
    for (const List<Dictionary>::Element* el = events.front(); el != 0; el = el->next())
        add_event(el->get());
}

Error Gdga::submit_events()
{
    print("gdga - submitting events");

    MutexLock mutex_lock(mutex);

    if (!FileAccess::exists(EVENTS_FILE))
    {
        print("gdga - no events (cannot find file for reading events)");
        return ERR_FILE_NOT_FOUND;
    }

    Error err;
    FileAccess* file = FileAccess::open(EVENTS_FILE, FileAccess::READ_WRITE, &err);

    if (err != OK)
    {
        print("gdga - cannot open file for reading events");
        return err;
    }

    print("gdga - reading events from file");

    List<Dictionary> events;

    while (!file->eof_reached())
    {
        String line = file->get_line().c_unescape();
        if (line.size() > 0)
        {
            Variant event = parse_json(line);
            if (event.get_type() == Variant::DICTIONARY)
                events.push_back((Dictionary) event);
        }
    }

    file->close();

    DirAccess* dir = DirAccess::open("user://");
    dir->remove(EVENTS_FILE);

    mutex->unlock();

    update_session_id(events);

    Error submit_err = instant_submit_events(events);

    if (submit_err != OK)
    {
        print("gdga - cannot submit events");
        add_events(events);
        return submit_err;
    }

    print("gdga - events sent");

    return OK;
}

void Gdga::update_session_id(List<Dictionary>& events) const
{
    for (List<Dictionary>::Element* el = events.front(); el != 0; el = el->next())
    {
        el->get()["session_id"] = session_id;
    }
}

Error Gdga::instant_submit_events(const List<Dictionary>& events)
{
    print("gdga - events to be send " + String::num(events.size()));

    StringBuffer<512> buffer;

    buffer += "[";

    for (const List<Dictionary>::Element* el = events.front(); el != 0; el = el->next())
    {
        buffer += JSON::print(el->get());

        if (el->next() != 0)
            buffer += ", ";
    }

    buffer += "]";

    return send(buffer.as_string(),
                get_setting("gdga/endpoint_addr", DEFAULT_ENDPOINT_ADDR),
                get_events_route(),
                last_response);
}

Error Gdga::init_request()
{
    print("gdga - sending init request");

    Dictionary json;
    json["platform"] = device_info.platform;
    json["os_version"] = device_info.os_version;
    json["sdk_version"] = device_info.sdk_version;

    String response;
    Error err = send(JSON::print(json),
                     get_setting("gdga/endpoint_addr", DEFAULT_ENDPOINT_ADDR),
                     get_init_route(),
                     response);
    last_response = response;

    if (err == OK)
    {
        Variant res;
        String res_r_err;
        int res_err_line;

        Error res_err = JSON::parse(response, res, res_r_err, res_err_line);

        if (res_err != OK || res.get_type() != Variant::DICTIONARY)
            return FAILED;

        Dictionary dict = Dictionary(res);

        if (dict.has("server_ts"))
        {
            last_server_ts = String(dict["server_ts"]).to_int64();
            last_offset = last_server_ts - OS::get_singleton()->get_unix_time();
        }

        if (dict.has("enabled"))
            last_server_enabled = String(dict["enabled"]).to_lower() == "true";

        session_id = uuid();

        print("gdga - init request sent");

        print("gdga - session id: " + session_id);

        return OK;
    }

    return err;
}

String Gdga::get_init_route() const
{
    return "/v2/" + get_setting("gdga/game_key", DEFAULT_GAME_KEY) + "/init";
}

String Gdga::get_events_route() const
{
    return "/v2/" + get_setting("gdga/game_key", DEFAULT_GAME_KEY) + "/events";
}

String Gdga::get_ab_test_route() const
{
    return "/v2/command_center?game_key=" + get_setting("gdga/game_key", DEFAULT_GAME_KEY)
            + "&interval_seconds=1000000";
}

int64_t Gdga::get_client_ts() const
{
    return OS::get_singleton()->get_unix_time() + last_offset;
}

Variant Gdga::parse_json(const String& json) const
{
    Variant ret;
    String err_str;
    int err_line;
    int err = JSON::parse(json, ret, err_str, err_line);
    if (err != OK)
        ret = 0;
    return ret;
}

Dictionary Gdga::merge_dict(const Dictionary& d1, const Dictionary& d2) const
{
    Dictionary merged = d1.duplicate();
    Array keys = d2.keys();
    for (int i = 0; i < keys.size(); i++)
        merged[keys[i]] = d2[keys[i]];
    return merged;
}

String Gdga::get_last_response() const
{
    return last_response;
}

void Gdga::add_log(const String& state)
{
    logs.push_back(state);

    if (logs.size() > 32)
        logs.pop_front();
}

String Gdga::get_log() const
{
    StringBuffer<512> state;

    state += "name: [" + device_info.name + "]\n";
    state += "platform: [" + device_info.platform + "]\n";
    state += "os_version: [" + device_info.os_version + "]\n";
    state += "user_id: [" + device_info.user_id + "]\n";
    state += "manufacturer: [" + device_info.manufacturer + "]\n";
    state += "build: [" + get_setting("gdga/build", String("")) + "]\n\n";

    for (const List<String>::Element* el = logs.front(); el != 0; el = el->next())
        state += el->get() + "\n";

    return state.as_string();
}

void Gdga::set_custom_field(int num, const String& value)
{
    switch (num)
    {
    case 1:
        custom_field_1 = value;
        break;
    case 2:
        custom_field_2 = value;
        break;
    case 3:
        custom_field_3 = value;
        break;
    }
}

void Gdga::set_abtest_config(const String& name, const String& value)
{
    abtest_config_name = name;
    abtest_config_value = value;
}

Dictionary Gdga::annotations() const
{
    Dictionary ann;
    ann["device"] = device_info.name;
    ann["user_id"] = device_info.user_id;
    ann["sdk_version"] = device_info.sdk_version;
    ann["os_version"] = device_info.os_version;
    ann["manufacturer"] = device_info.manufacturer;
    ann["platform"] = device_info.platform;
    ann["session_id"] = session_id;
    ann["session_num"] = session_num;
    ann["client_ts"] = get_client_ts();
    ann["v"] = 2;

    if (abtest_config_name != "")
    {
        Dictionary abtest;
        abtest[abtest_config_name] = abtest_config_value;
        ann["configurations"] = abtest;
    }

    if (custom_field_1 != "")
        ann["custom_01"] = custom_field_1;

    if (custom_field_2 != "")
        ann["custom_02"] = custom_field_2;

    if (custom_field_3 != "")
        ann["custom_03"] = custom_field_3;

    String build = get_setting("gdga/build", String(""));
    if (build != "")
        ann["build"] = build;

    return ann;
}

void Gdga::crash() const
{
    volatile int* a = reinterpret_cast<volatile int*> (0);
    *a = 1;
}

Dictionary Gdga::fetch_ab_test_config()
{

    Dictionary json;
    json["platform"] = device_info.platform;
    json["os_version"] = device_info.os_version;
    json["sdk_version"] = device_info.sdk_version;
    json["user_id"] = device_info.user_id;

    String response;
    Error err = send(JSON::print(json),
                     "https://rubick.gameanalytics.com",
                     get_ab_test_route(),
                     response);

    last_response = response;

    Dictionary dict;

    if (err == OK)
    {
        Variant res;
        String res_r_err;
        int res_err_line;

        Error res_err = JSON::parse(response, res, res_r_err, res_err_line);

        if (res_err != OK || res.get_type() != Variant::DICTIONARY)
            return dict;

        return Dictionary(res);
    }

    return dict;
}

void Gdga::_bind_methods()
{
    ClassDB::bind_method(D_METHOD("set_session_num"), &Gdga::set_session_num);
    ClassDB::bind_method(D_METHOD("init_request"), &Gdga::init_request);
    ClassDB::bind_method(D_METHOD("add_event"), &Gdga::add_event);
    ClassDB::bind_method(D_METHOD("submit_events"), &Gdga::submit_events);
    ClassDB::bind_method(D_METHOD("get_last_response"), &Gdga::get_last_response);
    ClassDB::bind_method(D_METHOD("add_log"), &Gdga::add_log);
    ClassDB::bind_method(D_METHOD("set_abtest_config"), &Gdga::set_abtest_config);
    ClassDB::bind_method(D_METHOD("set_custom_field"), &Gdga::set_custom_field);
    ClassDB::bind_method(D_METHOD("crash"), &Gdga::crash);
    ClassDB::bind_method(D_METHOD("fetch_ab_test_config"), &Gdga::fetch_ab_test_config);
}
