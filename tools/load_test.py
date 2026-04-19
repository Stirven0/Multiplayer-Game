#!/usr/bin/env python3
"""
Test de carga: Simula N jugadores concurrentes
"""

import websocket
import json
import threading
import time
import random
import sys

class BotPlayer(threading.Thread):
    def __init__(self, player_num, url="ws://localhost:8080"):
        super().__init__(daemon=True)
        self.player_num = player_num
        self.url = url
        self.ws = None
        self.running = False
        self.messages_received = 0
        self.errors = []
        
    def run(self):
        try:
            self.ws = websocket.create_connection(self.url)
            self.running = True
            
            # Login
            self._send({
                "type": "LOGIN_REQUEST",
                "username": f"bot_{self.player_num}",
                "password": f"pass{self.player_num}"
            })
            
            # Esperar respuesta (simplificado)
            time.sleep(0.5)
            
            # Unirse a sala común o crear
            if self.player_num == 0:
                self._send({"type": "CREATE_ROOM", "mapId": "map_01"})
                time.sleep(0.5)
                self._send({"type": "GAME_START"})
            
            time.sleep(0.3 * self.player_num)  # Staggered join
            
            # Loop de juego
            start_time = time.time()
            last_shot = 0
            
            while self.running and time.time() - start_time < 30:  # 30 segundos
                # Movimiento aleatorio
                self._send({
                    "type": "MOVE_INPUT",
                    "dx": random.uniform(-1, 1),
                    "dy": random.uniform(-1, 1),
                    "sprinting": random.random() > 0.8
                })
                
                # Disparo ocasional
                if time.time() - last_shot > 0.5:
                    self._send({
                        "type": "SHOOT_INPUT",
                        "angle": random.uniform(0, 6.28)
                    })
                    last_shot = time.time()
                
                # Recibir mensajes pendientes (no bloqueante)
                self.ws.settimeout(0.05)
                try:
                    msg = self.ws.recv()
                    self.messages_received += 1
                except websocket.WebSocketTimeoutException:
                    pass
                
                time.sleep(0.05)  # 20Hz
                
        except Exception as e:
            self.errors.append(str(e))
        finally:
            if self.ws:
                self.ws.close()
    
    def _send(self, data):
        if self.ws:
            self.ws.send(json.dumps(data))
    
    def stop(self):
        self.running = False

def run_load_test(num_players=10):
    print(f"[*] Iniciando test de carga con {num_players} jugadores...")
    
    bots = [BotPlayer(i) for i in range(num_players)]
    
    start = time.time()
    for bot in bots:
        bot.start()
        time.sleep(0.1)  # Evitar thundering herd
    
    # Monitorear
    try:
        while time.time() - start < 35:
            alive = sum(1 for b in bots if b.is_alive())
            total_msgs = sum(b.messages_received for b in bots)
            print(f"\r[*] Vivos: {alive}/{num_players} | Mensajes recibidos: {total_msgs}", end="")
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n[!] Deteniendo...")
    
    # Resultados
    for bot in bots:
        bot.stop()
        bot.join(timeout=2)
    
    print(f"\n\n[+] Resultados:")
    print(f"    Tiempo total: {time.time() - start:.1f}s")
    print(f"    Bots finalizados: {sum(1 for b in bots if not b.is_alive())}")
    print(f"    Total mensajes: {sum(b.messages_received for b in bots)}")
    errors = [e for b in bots for e in b.errors]
    if errors:
        print(f"    Errores: {len(errors)}")
        for e in errors[:5]:
            print(f"      - {e}")

if __name__ == "__main__":
    num = int(sys.argv[1]) if len(sys.argv) > 1 else 10
    run_load_test(num)

