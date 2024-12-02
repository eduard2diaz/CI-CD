// Importa el módulo http de Node.js
const http = require('http');

// Define el puerto donde se ejecutará el servidor
const PORT = process.env.PORT;

// Crea el servidor
const server = http.createServer((req, res) => {
  // Establece el encabezado de respuesta como texto plano
  res.writeHead(200, { 'Content-Type': 'text/plain' });
  // Envía el mensaje "Hola Mundo"
  res.end('¡Hola Mundo!');
});

// Inicia el servidor y escucha en el puerto definido
server.listen(PORT, () => {
  console.log(`Servidor escuchando en http://localhost:${PORT}`);
});
