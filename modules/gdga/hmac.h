#ifndef __HMAC_H__
#define __HMAC_H__

#include "../../core/ustring.h"
#include "../../core/variant.h"

String hmac_sha_256(const String& data, const String& secret);
String hmac_sha_256(const PoolVector<uint8_t>& data, const String& secret);

#endif

