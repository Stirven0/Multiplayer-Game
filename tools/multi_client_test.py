#!/usr/bin/env python3
"""
Multi client test harness for WebSocket server.
Usage: python multi_client_test.py
"""
import websocket
import threading
import json
import time
import uuid

class TestClient:
    def __init__(self, url="ws://localhost:8080"):
        self.url = url
        self.ws = None
        self.running = False
        self.recv_thread = None
        self.id = str(uuid.uuid4())[:8]

    def connect(self):
        print(f"[{self.id}] Conectando a {self.url}")
        if self.ws and self.ws.connected:
            print(f"[{self.id}] Ya conectado")
            return True
        try:
            self.ws = websocket.create_connection(self.url, timeout=5)
            self.running = True
            self.recv_thread = threading.Thread(target=self._receive_loop, daemon=True)
            self.recv_thread.start()
            return True
        except Exception as e:
            print(f"[{self.id}] Error de conexión: {e}")
            return False

    def _receive_loop(self):
        while self.running and self.ws:
            try:
                msg = self.ws.recv()
                if msg is None:
                    continue
                try:
                    parsed = json.loads(msg)
                    print(f"\n[{self.id} ←] {json.dumps(parsed, ensure_ascii=False)}")
                except Exception:
                    print(f"\n[{self.id} ←] {msg}")
            except websocket.WebSocketConnectionClosedException:
                print(f"\n[{self.id}] Conexión cerrada por el servidor")
                self.running = False
                break
            except Exception as e:
                print(f"\n[{self.id}] Error recibiendo: {e}")
                break

    def send(self, obj):
        if not self.ws or not self.running:
            print(f"[{self.id}] No conectado")
            return
        try:
            s = json.dumps(obj)
            self.ws.send(s)
            print(f"[{self.id} →] {s}")
        except Exception as e:
            print(f"[{self.id}] Error enviando: {e}")

    def send_raw(self, raw_str):
        if not self.ws or not self.running:
            print(f"[{self.id}] No conectado")
            return
        try:
            self.ws.send(raw_str)
            print(f"[{self.id} → RAW] {raw_str}")
        except Exception as e:
            print(f"[{self.id}] Error enviando raw: {e}")

    def close(self):
        self.running = False
        try:
            if self.ws:
                self.ws.close()
                print(f"[{self.id}] Desconectado")
        except Exception as e:
            print(f"[{self.id}] Error cerrando: {e}")
        finally:
            self.ws = None

class ClientManager:
    def __init__(self):
        self.clients = {}  # id -> TestClient
        self.selected = None

    def create(self, url="ws://localhost:8080"):
        c = TestClient(url)
        ok = c.connect()
        if ok:
            self.clients[c.id] = c
            self.selected = c.id
        return c.id if ok else None

    def list(self):
        if not self.clients:
            print("No hay clientes")
            return
        for cid, c in self.clients.items():
            status = "OPEN" if c.ws and c.ws.connected else "CLOSED"
            sel = "*" if cid == self.selected else " "
            print(f"{sel} {cid} - {status} - {c.url}")

    def select(self, cid):
        if cid in self.clients:
            self.selected = cid
            print(f"Seleccionado {cid}")
        else:
            print("ID no encontrado")

    def send_selected(self, obj):
        if not self.selected:
            print("No hay cliente seleccionado")
            return
        c = self.clients.get(self.selected)
        if c:
            c.send(obj)

    def send_raw_selected(self, raw):
        if not self.selected:
            print("No hay cliente seleccionado")
            return
        c = self.clients.get(self.selected)
        if c:
            c.send_raw(raw)

    def disconnect(self, cid):
        c = self.clients.pop(cid, None)
        if c:
            c.close()
            if self.selected == cid:
                self.selected = next(iter(self.clients), None)
            print(f"{cid} eliminado")
        else:
            print("ID no encontrado")

    def disconnect_all(self):
        for cid in list(self.clients.keys()):
            self.disconnect(cid)

def print_help():
    print("""
Comandos:
  create [url]           - Crear y conectar nuevo cliente (por defecto ws://localhost:8080)
  list                   - Listar clientes
  select <id>            - Seleccionar cliente por id
  send <json>            - Enviar JSON desde cliente seleccionado (ej: send {"type":"PING"})
  raw <string>           - Enviar string raw desde cliente seleccionado
  login <user> <pass>    - Enviar LOGIN_REQUEST desde seleccionado
  move <dx> <dy>         - Enviar MOVE_INPUT desde seleccionado
  disconnect <id>        - Desconectar y eliminar cliente
  disconnect_all         - Desconectar todos
  help                   - Mostrar ayuda
  quit                   - Salir
""")

def interactive_shell():
    mgr = ClientManager()
    print("Multi client test harness. Escribe 'help' para ver comandos.")
    while True:
        try:
            line = input("> ").strip()
            if not line:
                continue
            parts = line.split(maxsplit=1)
            cmd = parts[0].lower()
            arg = parts[1] if len(parts) > 1 else ""

            if cmd == "create":
                url = arg or "ws://localhost:8080"
                cid = mgr.create(url)

            elif cmd == "list":
                mgr.list()

            elif cmd == "select":
                mgr.select(arg)

            elif cmd == "send":
                if not arg:
                    print("Uso: send <json>")
                    continue
                try:
                    obj = json.loads(arg)
                except Exception as e:
                    print("JSON inválido:", e)
                    continue
                mgr.send_selected(obj)

            elif cmd == "raw":
                mgr.send_raw_selected(arg)

            elif cmd == "login":
                parts2 = arg.split()
                if len(parts2) < 2:
                    print("Uso: login <user> <pass>")
                    continue
                user, pwd = parts2[0], parts2[1]
                mgr.send_selected({"type":"LOGIN_REQUEST","username":user,"password":pwd})

            elif cmd == "move":
                parts2 = arg.split()
                if len(parts2) < 2:
                    print("Uso: move <dx> <dy>")
                    continue
                dx, dy = float(parts2[0]), float(parts2[1])
                mgr.send_selected({"type":"MOVE_INPUT","dx":dx,"dy":dy})

            elif cmd == "disconnect":
                if not arg:
                    print("Uso: disconnect <id>")
                    continue
                mgr.disconnect(arg)

            elif cmd == "disconnect_all":
                mgr.disconnect_all()

            elif cmd == "help":
                print_help()

            elif cmd == "quit":
                mgr.disconnect_all()
                break

            else:
                print("Comando desconocido. Escribe 'help'.")

        except KeyboardInterrupt:
            print("\nInterrumpido por usuario")
            mgr.disconnect_all()
            break
        except Exception as e:
            print("Error:", e)

if __name__ == "__main__":
    interactive_shell()
