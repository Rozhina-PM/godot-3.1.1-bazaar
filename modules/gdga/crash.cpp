#include "crash.h"
#include "gdga.h"
#include "../../core/io/json.h"
#include "../../core/print_string.h"
#include "../../core/string_buffer.h"
#include "../../core/version.h"
//#include "engine.h"
//#include <dlfcn.h>
//#include <unwind.h>
#include <signal.h>
#include <stdlib.h>

//struct BacktraceState
//{
//    void** current;
//    void** end;
//};
//
//static _Unwind_Reason_Code unwind_callback(struct _Unwind_Context* context, void* arg)
//{
//    BacktraceState* state = static_cast<BacktraceState*> (arg);
//    uintptr_t pc = _Unwind_GetIP(context);
//    if (pc)
//    {
//        if (state->current == state->end)
//            return _URC_END_OF_STACK;
//        else
//            *state->current++ = reinterpret_cast<void*> (pc);
//    }
//    return _URC_NO_REASON;
//}
//
//size_t capture_backtrace(void** buffer, size_t max)
//{
//    BacktraceState state = {buffer, buffer + max};
//    _Unwind_Backtrace(unwind_callback, &state);
//
//    return state.current - buffer;
//}
//
//String dump_backtraces(void** buffer, size_t count)
//{
//    StringBuffer<512> output;
//
//    for (size_t idx = 0; idx < count; ++idx)
//    {
//        const void* addr = buffer[idx];
//        const char* symbol = "";
//
//        Dl_info info;
//        if (dladdr(addr, &info) && info.dli_sname)
//            symbol = info.dli_sname;
//
//        char entry[512];
//        sprintf(entry, "%lu: %p %s\n", idx, addr, symbol);
//
//        output += entry;
//    }
//
//    return output.as_string();
//}
//    const size_t max = 30;
//    void* buffer[max];
//    String dump = dump_backtraces(buffer, capture_backtrace(buffer, max));

static void handle_crash(int signal)
{
    print_line("-- REPORT CRASH: BEGIN --");
    
    Gdga* gdga = Gdga::get_singleton();

    if (!gdga)
        return;
    
    Dictionary event;
    event["category"] = "error";
    event["severity"] = "critical";
    event["message"] = gdga->get_log();
    
    print_line(event["message"]);
    
    gdga->init_request();
    gdga->add_event(event);
    gdga->submit_events();
    print_line("-- REPORT CRASH: END --");

//    abort();
}

void crash_handler(bool enable)
{
    if (enable)
    {
        signal(SIGSEGV, handle_crash);
        signal(SIGFPE, handle_crash);
        signal(SIGILL, handle_crash);
    }
    else
    {
        signal(SIGSEGV, 0);
        signal(SIGFPE, 0);
        signal(SIGILL, 0);
    }
}
