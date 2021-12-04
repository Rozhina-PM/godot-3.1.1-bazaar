#include "hmac.h"
#include "../../core/vector.h"
#include "../../thirdparty/misc/sha256.h"
#include "../../thirdparty/misc/base64.h"
#include "../../core/version.h"

template<typename T1, typename T2>
void assign(Vector<T1>& dst, const T2& src)
{
    dst.clear();
    dst.resize(src.size());
    for (int i = 0; i < src.size(); i++)
        dst.write[i] = src[i];
}

Vector<uint8_t> sha256(const Vector<uint8_t>& input)
{
    unsigned char hash[32];
    sha256_context ctx;
    sha256_init(&ctx);
    sha256_hash(&ctx, (uint8_t*) input.ptr(), input.size());
    sha256_done(&ctx, hash);
    Vector<uint8_t> ret;
    ret.resize(32);
    for (int i = 0; i < 32; i++)
        ret.write[i] = hash[i];
    return ret;
}

template <typename T1, typename T2>
Vector<T1> concat(const Vector<T1>& v1, const Vector<T2>& v2)
{
    Vector<T1> ret;
    ret.resize(v1.size() + v2.size());
    for (int i = 0; i < v1.size(); i++)
        ret.write[i] = v1[i];
    for (int i = 0; i < v2.size(); i++)
        ret.write[i + v1.size()] = v2[i];
    return ret;
}

template <typename T>
Vector<T> concat(const Vector<T>& v1, const PoolVector<uint8_t>& v2)
{
    PoolVector<uint8_t>::Read rv2 = v2.read();

    Vector<T> ret;
    ret.resize(v1.size() + v2.size());
    for (int i = 0; i < v1.size(); i++)
        ret.write[i] = v1[i];
    for (int i = 0; i < v2.size(); i++)
        ret.write[i + v1.size()] = rv2[i];
    return ret;
}

String hmac_sha_256(const String& data, const String& secret)
{
    Vector<uint8_t> key;

    if (secret.length() <= 64)
        assign(key, secret.utf8());
    else
        assign(key, secret.sha256_buffer());

    while (key.size() < 64)
        key.push_back(0);

    Vector<uint8_t> i_key;
    Vector<uint8_t> o_key;

    for (int i = 0; i < 64; i++)
    {
        o_key.push_back(key[i] ^ 0x5c);
        i_key.push_back(key[i] ^ 0x36);
    }

    Vector<uint8_t> msg;
    assign(msg, data.utf8());

    if (msg.size() > 0)
        msg.remove(msg.size() - 1);

    Vector<uint8_t> digest = sha256(concat(o_key, sha256(concat(i_key, msg))));

    char base64[512];
    base64_encode(base64, (char*) digest.ptr(), digest.size());

    return String(base64);
}

String hmac_sha_256(const PoolVector<uint8_t>& data, const String& secret)
{
    Vector<uint8_t> key;

    if (secret.length() <= 64)
        assign(key, secret.utf8());
    else
        assign(key, secret.sha256_buffer());

    while (key.size() < 64)
        key.push_back(0);

    Vector<uint8_t> i_key;
    Vector<uint8_t> o_key;

    for (int i = 0; i < 64; i++)
    {
        o_key.push_back(key[i] ^ 0x5c);
        i_key.push_back(key[i] ^ 0x36);
    }

    Vector<uint8_t> digest = sha256(concat(o_key, sha256(concat(i_key, data))));

    char base64[512];
    base64_encode(base64, (char*) digest.ptr(), digest.size());

    return String(base64);
}
