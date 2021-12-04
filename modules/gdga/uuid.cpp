#include "uuid.h"
#include "../../core/vector.h"
#include "../../core/version.h"
#include "../../core/math/math_funcs.h"
#include <stdio.h>

Vector<uint8_t> random_bytes(int n)
{
    Vector<uint8_t> bytes;
    bytes.resize(n);

    for (int i = 0; i < n; i++)
        bytes.write[i] = Math::rand() % 256;
    return bytes;
}

Vector<uint8_t> uuid_bin()
{
    Vector<uint8_t> b = random_bytes(16);
    b.write[6] = (b[6] & 0x0f) | 0x40;
    b.write[8] = (b[8] & 0x3f) | 0x81;
    return b;
}

String uuid()
{
    Vector<uint8_t> b = uuid_bin();

    char result[512];

    sprintf(result, "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
            b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10], b[11], b[12], b[13], b[14], b[15]);

    return String(result);
}
