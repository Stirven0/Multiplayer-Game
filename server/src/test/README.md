# Todos los tests
mvn test -pl server

# Solo tests unitarios (excluir integración si es lenta)
mvn test -pl server -Dtest="!*IntegrationTest"

# Un paquete específico
mvn test -pl server -Dtest="com.aa.server.game.system.*"

# Con cobertura (si añades JaCoCo)
mvn test -pl server -Djacoco.skip=false
