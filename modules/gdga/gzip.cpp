#include "gzip.h"
#include "../../core/io/compression.h"
//#include "../../thirdparty/zlib/zlib.h"

PoolVector<uint8_t> gzip(const String& input)
{
    CharString body_bytes = input.utf8();
    int in_len = body_bytes.size() > 0 ? body_bytes.size() - 1 : 0;
    PoolVector<uint8_t> gziped;
    gziped.resize(Compression::get_max_compressed_buffer_size(in_len, Compression::MODE_GZIP));
    int out_len = Compression::compress(gziped.write().ptr(), (uint8_t*) body_bytes.ptr(), in_len, Compression::MODE_GZIP);
    gziped = gziped.subarray(0, out_len - 1);
    return gziped;
}

//PoolVector<uint8_t> gzip(const String& input)
//{
//    CharString body_bytes = input.utf8();
//
//    PoolVector<uint8_t> gziped;
//    gziped.resize(body_bytes.size() * 2 + 32);
//
//    z_stream defstream;
//    defstream.zalloc = Z_NULL;
//    defstream.zfree = Z_NULL;
//    defstream.opaque = Z_NULL;
//    defstream.avail_in = body_bytes.size() > 0 ? body_bytes.size() - 1 : 0;
//    defstream.next_in = (Bytef *) body_bytes.ptr();
//    defstream.avail_out = (uInt) gziped.size();
//    defstream.next_out = (Bytef *) gziped.write().ptr();
//
//    deflateInit2(&defstream, Z_BEST_SPEED, Z_DEFLATED, 15 | 16, 8, Z_DEFAULT_STRATEGY);
//    deflate(&defstream, Z_FINISH);
//    deflateEnd(&defstream);
//
//    gziped = gziped.subarray(0, defstream.total_out - 1);
//    
//    Error err;
//    FileAccess* file;
//    file = FileAccess::open("user://zzz.dat", FileAccess::WRITE, &err);
//    file->store_buffer(gziped.read().ptr(), gziped.size());
//    file->close();
//
//    return gziped;
//}
