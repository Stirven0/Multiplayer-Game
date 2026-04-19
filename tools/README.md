# 1. Instalar dependencia
pip install websocket-client

# 2. Asegurar que el servidor corre
cd multiplayer-game
mvn clean package -DskipTests
java -jar server/target/server-1.0-SNAPSHOT.jar

# 3. En otra terminal, ejecutar test client
python tools/test_client.py

# Ejemplo de sesión interactiva:
> connect
> login player1 pass1
> create
> start
> move 0.5 0.0      # Mover derecha
> move 0.0 0.5      # Mover abajo
> shoot 0           # Disparar derecha
> shoot 1.57        # Disparar abajo (90°)
> auto              # Bot automático

# 4. En otra terminal, ejecuta load test

python tools/load_test.py 5    # 5 jugadores
python tools/load_test.py 20   # 20 jugadores (stress test)
