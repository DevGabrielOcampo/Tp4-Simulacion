import { useState } from 'react';
import Datos from './components/ObtenerSimulacion/ObtenerSimulacion';
import TablaSimulacion from './components/TablaSimulacion/TablaSimulacion';

function App() {
  const [datosGenerados, setDatosGenerados] = useState(null);

  return (
    <div >
      {/* Enviamos setDatosGenerados a Datos para actualizar el estado en App */}
      <Datos setDatosGenerados={setDatosGenerados} />

      {/* Si los datos fueron generados, los pasamos a Histograma */}
      {datosGenerados ? (
        <>          
          <TablaSimulacion data={datosGenerados.data}  // Pasamos los datos generados
            numBins={parseInt(datosGenerados.intervalos, 10)}  // Pasamos la cantidad de intervalos
          />          
        </>
      ) : null} {/* Eliminamos el <p>Cargando datos...</p> */}
    </div>
  );
}

export default App;

