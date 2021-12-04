#ifndef __GDGA_H__
#define __GDGA_H__

#include "../../core/object.h"
#include "../../core/vector.h"
#include "../../core/dictionary.h"
#include "../../core/error_list.h"
#include "../../core/os/mutex.h"

class Gdga : public Object
{
    GDCLASS(Gdga, Object);
    
    static Gdga* singleton;
    
    const String EVENTS_FILE = "user://gdga-events.dat";

    String last_response;
    bool last_server_enabled;
    int64_t last_server_ts;
    int64_t last_offset;
    int session_num;
    String session_id;
    List<String> logs;
    Mutex *mutex;
    
    String abtest_config_name;
    String abtest_config_value;
    
    String custom_field_1;
    String custom_field_2;
    String custom_field_3;

    struct
    {
        String name;
        String user_id;
        String sdk_version;
        String os_version;
        String manufacturer;
        String platform;
    } device_info;
    
    template<typename T>
    T get_setting(const String& name, const T& def) const;

    String get_init_route() const;
    String get_events_route() const;
    String get_ab_test_route() const;
    int64_t get_client_ts() const;
    Dictionary merge_dict(const Dictionary& d1, const Dictionary& d2) const;
    Error instant_submit_events(const List<Dictionary>& events);
    void update_session_id(List<Dictionary>& events) const;
    Error send(const String& body, const String& endpoint_addr, const String& route, String& response);
    void add_events(const List<Dictionary>& events);
    Variant parse_json(const String& json) const;
    void query_device_info();
    void print(const String& str) const;

protected:
    static void _bind_methods();

public:
    Gdga();
    static Gdga *get_singleton() { return singleton; }
    void crash() const;
    void set_session_num(int num);
    void add_log(const String& state);
    void set_abtest_config(const String& name, const String& value);
    void set_custom_field(int num, const String& value);
    String get_log() const;
    Error add_event(const Dictionary& event);
    Error init_request();
    Error submit_events();
    Dictionary fetch_ab_test_config();
    String get_last_response() const;
    Dictionary annotations() const;
};

#endif
