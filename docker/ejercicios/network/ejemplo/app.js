const axios = require('axios');

// URL remota que quieres llamar
const url = 'https://jsonplaceholder.typicode.com/posts'; // Ejemplo de una URL pública

// Función para hacer la solicitud y manejar la respuesta
async function callRemoteUrl() {
  try {
    const response = await axios.get(url);
    console.log('Respuesta de la URL remota:', response.data);
  } catch (error) {
    console.error('Error al llamar a la URL:', error);
  }
}

// Llamada a la función
callRemoteUrl();
