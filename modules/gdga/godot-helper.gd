extends Node

const USER_EVENT = {
	"category": "user"
}

const RESOURCE_EVENT = {
	"category": "resource",
	"event_id": "",
	"amount": 0.0
}

const SESSION_END_EVENT = {
	"category": "session_end",
	"length": 1 # secs
}

const PROGRESSION_EVENT = {
	"category": "progression",
	"event_id": "",
#	"attempt_num": 0,
#	"score": 0
}

const DESIGN_TEST_EVENT = {
	"category": "design",
	"event_id": ""
#	"value": 0
}

const BUSSINESS_EVENT =  {
	"category": "business",
	"amount": 0,
	"currency": "IRR",
	"event_id": "",
	"transaction_num": 0
#	"cart_type": "",
#	"receipt_info": {"receipt": "xyz", "store": "apple"}
}

const ERROR_EVENT = {
	"category": "error",
	"severity": "debug", # debug,info,warning,error,critical
	"message": ""
}

var GA = null
var ga_mutex
var ga_thread_init
var ga_thread_submit
var ga_submit_again = 0
var session_epoch = 0

func _ready():
	if Engine.get_singleton("Gdga"):
		GA = Engine.get_singleton("Gdga")
		GA.setup("api.gameanalytics.com", "44e4ee5a7f9d38b996c1389765ae0981", "228df76c06cf842fc57a0d9561822b0a5097b6f0")
		GA.set_session_num(GameData.get_item("sessions"))
		GA.enable_crash_handler(true)
		ga_mutex = Mutex.new()
		ga_thread_init = Thread.new()
		ga_thread_submit = Thread.new()
		session_epoch = OS.get_unix_time()
		init_request()

func add_event(event):
	if GA == null:
		return
	GA.add_event(event)

func add_progression_start_event(level_no):
	var event = PROGRESSION_EVENT
	event["event_id"] = "Start:" + str(level_no)
	add_event(event)

func add_progression_fail_event(level_no):
	var event = PROGRESSION_EVENT
	event["event_id"] = "Fail:" + str(level_no)
	add_event(event)

func add_progression_complete_event(level_no, score):
	var event = PROGRESSION_EVENT
	event["event_id"] = "Complete:" + str(level_no)
	event["score"] = score
	add_event(event)

func add_session_end_event(length):
	var event = SESSION_END_EVENT
	event["length"] = length
	add_event(event)

func add_user_event():
	add_event(USER_EVENT)

func add_bussiness_event(bundle, amount, transaction):
	var event = BUSSINESS_EVENT
	event['event_id'] = "Bundle:" + bundle
	event['amount'] = amount
	event['transaction_num'] = transaction
	add_event(event)

func init_request():
	if GA == null:
		return

#	if OS.is_debug_build():
#		return

	if ga_thread_init.is_active():
		return
	ga_thread_init.start(self, "_init_request")

func submit_events():
	if GA == null:
		return

#	if OS.is_debug_build():
#		return

	ga_submit_again += 1
	if ga_thread_submit.is_active():
		return
	ga_thread_submit.start(self, "_submit_events")

func _init_request(unused):
	ga_mutex.lock()
	var ee = GA.init_request()
	ga_thread_init.call_deferred("wait_to_finish")
	ga_mutex.unlock()

func _submit_events(unused):
	ga_mutex.lock()
	while ga_submit_again > 0:
		var e = GA.submit_events()
		ga_submit_again -= 1
	ga_submit_again = max(0, ga_submit_again)

	ga_thread_submit.call_deferred("wait_to_finish")
	ga_mutex.unlock()

func _notification(what):
	if what == MainLoop.NOTIFICATION_WM_QUIT_REQUEST or what == MainLoop.NOTIFICATION_WM_FOCUS_OUT:
		var session_duration = (OS.get_unix_time() - session_epoch)
		if session_duration > 0:
			add_session_end_event(session_duration)
	elif what == MainLoop.NOTIFICATION_WM_FOCUS_IN:
		session_epoch = OS.get_unix_time()

