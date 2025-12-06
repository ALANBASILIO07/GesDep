// Script para agregar un reporte de prueba a Firestore
// Ejecutar con: node add_test_report.js

const admin = require('firebase-admin');

// Inicializar Firebase Admin (ajustar la ruta del serviceAccountKey si es necesario)
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://gesdep.firebaseio.com'
});

const db = admin.firestore();

// Datos del reporte de prueba
const testReport = {
  reportId: 'TEST_REPORT_001',
  eventId: 'GENERAL',
  eventName: 'Aviso General',
  createdByUid: 'admin_test',
  createdByName: 'Administrador de Prueba',
  createdByRole: 'admin',
  subject: 'Mantenimiento de Cancha Principal',
  description: 'Se requiere pintar las líneas de la cancha de básquetbol principal. Las marcas están desgastadas y dificultan el juego.',
  photoUrls: [],
  category: 'Cancha sin pintar',
  priority: 'BAJA',
  status: 'Pendiente',
  adminResponse: null,
  respondedByUid: null,
  respondedByName: null,
  createdAt: Date.now(),
  updatedAt: Date.now()
};

// Agregar el reporte
db.collection('reports').doc('TEST_REPORT_001').set(testReport)
  .then(() => {
    console.log('✅ Reporte de prueba agregado exitosamente!');
    console.log('ID del Reporte:', testReport.reportId);
    console.log('Asunto:', testReport.subject);
    console.log('Prioridad:', testReport.priority);
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Error al agregar reporte:', error);
    process.exit(1);
  });
