#!/usr/bin/env python3
"""
Cliente de test manual para el servidor de juego.
Uso interactivo: python test_client.py
"""

import websocket
import json
import threading
import time
import sys

class GameTestClient:
    def __init__(self, url="ws://localhost:8080"):
        self.url = url
        self.ws = None
        self.player_id = None
        self.authenticated = False
        self.in_game = False
        self.running = False
        
    def connect(self):
        """Establece conexión WebSocket"""
        try:
            self.ws = websocket.create_connection(self.url)
            self.running = True
            
            # Hilo para recibir mensajes
            self.recv_thread = threading.Thread(target=self._receive_loop)
            self.recv_thread.daemon = True
            self.recv_thread.start()
            
            print(f"[+] Conectado a {self.url}")
            return True
        except Exception as e:
            print(f"[!] Error de conexión: {e}")
            return False
    
    def _receive_loop(self):
        """Loop de recepción en segundo plano"""
        while self.running and self.ws:
            try:
                msg = self.ws.recv()
                if msg:
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
        
        if msg_type == "GAME_STATE":
            # Resumen compacto del estado
            state = msg.get("gameState", {})
            players = state.get("players", [])
            bullets = state.get("bullets", [])
            tick = state.get("tick", 0)
            print(f"    Tick: {tick} | Jugadores: {len(players)} | Balas: {len(bullets)}")
            for p in players:
                pos = p.get("position", [0, 0])
                print(f"    - {p.get('id')[:8]}: HP={p.get('health'):.0f} @ ({pos[0]:.1f}, {pos[1]:.1f})")
        
        elif msg_type == "LOGIN_RESPONSE":
            self.authenticated = True
            print(f"    Token recibido: {msg.get('token', 'N/A')[:20]}...")
        
        elif msg_type == "ERROR":
            print(f"    Código: {msg.get('errorCode')}")
            print(f"    Mensaje: {msg.get('message')}")
            if msg.get("fatal"):
                print("    [FATAL] Desconectando...")
                self.running = False
        
        else:
            # Imprimir JSON completo para otros tipos
            print(f"    {json.dumps(msg, indent=2)[:200]}...")
    
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
    
    # ===== COMANDOS DE ALTO NIVEL =====
    
    def login(self, username, password):
        self.send({
            "type": "LOGIN_REQUEST",
            "username": username,
            "password": password
        })
    
    def create_room(self, map_id="map_01"):
        self.send({
            "type": "CREATE_ROOM",
            "mapId": map_id
        })
    
    def join_room(self, room_id):
        self.send({
            "type": "JOIN_ROOM",
            "roomId": room_id
        })
    
    def start_game(self):
        self.send({"type": "GAME_START"})
    
    def move(self, dx, dy, sprint=False):
        """Movimiento continuo - llamar repetidamente"""
        self.send({
            "type": "MOVE_INPUT",
            "dx": dx,
            "dy": dy,
            "sprinting": sprint
        })
    
    def shoot(self, angle):
        """Disparar en dirección (radianes)"""
        self.send({
            "type": "SHOOT_INPUT",
            "angle": angle
        })
    
    def move_to(self, target_x, target_y, duration=2.0):
        """Movimiento automatizado hacia coordenadas (simulación simple)"""
        # En un test real necesitarías conocer tu posición actual del GAME_STATE
        print(f"[+] Moviendo hacia ({target_x}, {target_y})...")
        start = time.time()
        while time.time() - start < duration and self.running:
            self.move(0.5, 0.5)  # Simplificado
            time.sleep(0.05)  # 20Hz como el servidor
    
    def disconnect(self):
        self.running = False
        if self.ws:
            self.ws.close()
        print("[+] Desconectado")


def interactive_shell():
    """Shell interactivo para testing manual"""
    client = GameTestClient()
    
    print("""
    ╔═══════════════════════════════════════╗
    ║  TEST CLIENT - Multiplayer Shooter    ║
    ╚═══════════════════════════════════════╝
    Comandos:
      connect                    - Conectar al servidor
      login <user> <pass>        - Autenticar
      create [map_id]            - Crear sala
      join <room_id>             - Unirse a sala
      start                      - Iniciar partida
      move <dx> <dy> [sprint]    - Enviar movimiento (ej: move 0.5 -0.5)
      shoot <angle>              - Disparar (radianes, 0=derecha)
      spam                       - Spam de movimiento (test carga)
      auto                       - Bot simple que se mueve y dispara
      quit                       - Salir
    """)
    
    while True:
        try:
            cmd = input("\n> ").strip().split()
            if not cmd:
                continue
            
            action = cmd[0].lower()
            
            if action == "quit":
                if client.running:
                    client.disconnect()
                break
                
            elif action == "connect":
                client.connect()
                
            elif action == "login":
                if len(cmd) < 3:
                    print("Uso: login <username> <password>")
                    continue
                client.login(cmd[1], cmd[2])
                
            elif action == "create":
                client.create_room(cmd[1] if len(cmd) > 1 else "map_01")
                
            elif action == "join":
                if len(cmd) < 2:
                    print("Uso: join <room_id>")
                    continue
                client.join_room(cmd[1])
                
            elif action == "start":
                client.start_game()
                
            elif action == "move":
                if len(cmd) < 3:
                    print("Uso: move <dx> <dy> [sprint]")
                    continue
                client.move(float(cmd[1]), float(cmd[2]), "sprint" in cmd)
                
            elif action == "shoot":
                if len(cmd) < 2:
                    print("Uso: shoot <angle>")
                    continue
                client.shoot(float(cmd[1]))
                
            elif action == "spam":
                print("[+] Enviando 100 mensajes de movimiento...")
                for i in range(100):
                    client.move(Math.sin(i/10), Math.cos(i/10))
                    time.sleep(0.01)
                    
            elif action == "auto":
                print("[+] Iniciando bot automático (Ctrl+C para detener)")
                import math
                try:
                    t = 0
                    while True:
                        client.move(math.sin(t), math.cos(t))
                        if t % 1 < 0.1:
                            client.shoot(t)
                        t += 0.2
                        time.sleep(0.05)
                except KeyboardInterrupt:
                    print("\n[+] Bot detenido")
                    
            else:
                print(f"Comando desconocido: {action}")
                
        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"[!] Error: {e}")

if __name__ == "__main__":
    import math  # Para el comando auto
    interactive_shell()

