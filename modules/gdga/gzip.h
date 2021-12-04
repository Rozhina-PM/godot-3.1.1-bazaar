#ifndef __GZIP_H__
#define __GZIP_H__

#include "../../core/variant.h"

PoolVector<uint8_t> gzip(const String& input);

#endif
