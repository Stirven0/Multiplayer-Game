import websocket
import threading
import json
class Ciente:
    def __init__(self, url="ws://localhost:8080"):
        self.url = url
        self.running = False
        
    def connect(self):
        """Establece la coneccion WebSocket"""
        try:
            self.ws = websocket.create_connection(self.url)
            self.running = True
            
            self.recv_thread = threading.Thread(target=self._receive_loop)
            self.recv_thread.daemon = True
            self.recv_thread.start()
            print(f"[+] Conectado a {self.url}")
            return True
        except Exception as e:
            print(f"[!] Error de conexion: {e}")
            return False
            
    def _receive_loop(self):
        """Loop de recepción en segundo plano"""
        while self.running and self.ws:
            try:
                msg = self.ws.recv()
                self._handle_message(json.loads(msg))
            except websocket.WebSocketConnectionClosedException:
                print("\n[!] Conexión cerrada por el servidor")
                self.running = False
                break
            except Exception as e:
                print(f"\n[!] Error recibiendo: {e}")
        
        
    def _handle_message(self, msg):
        """Procesa mensajes entrantes"""
        msg_type = msg.get("type", "UNKNOWN")
        
        # Formato coloreado según tipo
        colors = {
            "GAME_STATE": "\033[36m",      # Cyan
            "ERROR": "\033[31m",            # Rojo
            "LOGIN_RESPONSE": "\033[32m",   # Verde
            "default": "\033[33m"           # Amarillo
        }
        color = colors.get(msg_type, colors["default"])
        reset = "\033[0m"
        
        print(f"\n{color}[← {msg_type}]{reset}")
        
##############################################################
        
    def send(self, msg_dict):
        """Envía mensaje JSON al servidor"""
        if not self.ws or not self.running:
            print("[!] No conectado")
            return
        
        try:
            json_str = json.dumps(msg_dict)
            self.ws.send(json_str)
            print(f"\033[90m[→ {msg_dict.get('type')}]{json.dumps(msg_dict)[:80]}...\033[0m")
        except Exception as e:
            print(f"[!] Error enviando: {e}")