import thread
import tornado.web
import tornado.httpserver
import tornado.ioloop
import tornado.websocket
import tornado.options
import socket
import threading
import json

LISTEN_PORT = 8000
LISTEN_ADDRESS = '127.0.0.1'

WEB_SOCKET = None


class Queue:
    def __init__(self):
        self.lock_ = threading.Lock()
        self.read_, self.write_ = socket.socketpair()
        self.read_.setblocking(0)
        self.write_.setblocking(0)
        self.cmds_ = []

    def fileno(self):
        return self.read_.fileno()

    def finit(self):
        self.read_.close()
        self.write_.close()

    def push(self, cmd):
        self.lock_.acquire()
        self.cmds_.append(cmd)
        self.lock_.release()
        self.write_.send("1")

    def process(self):
        while True:
            if (len(self.read_.recv(1024)) < 1024):
                break

        self.lock_.acquire()
        cmds = self.cmds_
        self.cmds_ = []
        self.lock_.release()
        for c in cmds:
            try:
                c()
            except Exception as e:
                print("Exception " + str(e))


class EchoWebSocket(tornado.websocket.WebSocketHandler):
    def __init__(self, *args, **kwargs):
        self.handler_ = kwargs.pop("handler")
        super(EchoWebSocket, self).__init__(*args, **kwargs)

    def open(self):
        print("WebSocket opened")
        self.handler_.socket(self)

    def on_message(self, message):
        self.handler_.message(message)

    def on_close(self):
        print("WebSocket closed")
        self.handler_.socket(None)

    def check_origin(self, origin):
        return True


def server(core):
    # Create tornado application and supply URL routes
    app = tornado.web.Application([("/ws", EchoWebSocket, dict(handler=core))])

    # Setup HTTP Server
    http_server = tornado.httpserver.HTTPServer(app)
    http_server.listen(LISTEN_PORT, LISTEN_ADDRESS)

    io_loop = tornado.ioloop.IOLoop.current()
    io_loop.add_handler(core.fileno(), lambda *args: core.process(), io_loop.READ)

    # Start IO/Event loop
    tornado.ioloop.IOLoop.instance().start()


class Core:
    def __init__(self):
        self.queue_ = Queue()
        self.counter_ = 0
        self.handlers_ = {}
        self.socket_ = None

    def socket(self, s):
        self.socket_ = s

    def message(self, m):
        print(m)
        msg = json.loads(m)
        req_id = msg.get("req_id")
        if req_id:
            h = self.handlers_.get(req_id)
            if h:
                del self.handlers_[req_id]
                try:
                    h(json.loads(msg.get("resp")))
                except Exception as e:
                    print(e)

    def sendCmd(self, cmd, handler):
        self.queue_.push(lambda: self.sendImpl_(cmd, handler))

    def sendImpl_(self, cmd, handler):
        print("sendImpl_", cmd)
        if self.socket_:
            self.counter_ += 1
            self.handlers_[self.counter_] = handler
            self.socket_.write_message(json.dumps({"cmd": cmd, "req_id": self.counter_}))
        else:
            print("No Socket")

    def fileno(self):
        return self.queue_.fileno()

    def process(self):
        print("Process queue")
        self.queue_.process()

    def finit(self):
        self.queue_.finit()


def getResponseContainer(msg, *args):
    for a in args:
        container = msg.get(a)
        if container:
            return container
    return None


def baseHandler(msg):
    resp_container = getResponseContainer(msg, "history", "likes", "comments")
    if resp_container:
        print("Len " + str(len(resp_container)))


if __name__ == '__main__':
    core = Core()
    thread.start_new_thread(server, (core,))
    while True:
        s = str(raw_input("Cmd: "))
        if s == "exit":
            break
        elif s == "short":
            core.sendCmd("like_state M-mZ4S3eUKo QEspS6BG9S8 Fu6kqH4mvMk", baseHandler)
        else:
            core.sendCmd(s, baseHandler)
    core.finit()