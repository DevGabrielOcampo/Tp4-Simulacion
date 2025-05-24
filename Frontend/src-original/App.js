import { useState } from 'react';
import Datos from './components/ObtenerSerie/ObtenerSerie';
import Histograma from './components/Histograma/Histograma';
import MostrarSerie from './components/MostrarSerie/MostrarSerie'
import TablaFrecuencias from './components/TablaFrecuencia/TablaFrecuencia';

function App() {
  const [datosGenerados, setDatosGenerados] = useState(null);

  return (
    <div >
      {/* Enviamos setDatosGenerados a Datos para actualizar el estado en App */}
      <Datos setDatosGenerados={setDatosGenerados} />

      {/* Si los datos fueron generados, los pasamos a Histograma */}
      {datosGenerados ? (
        <>
          <Histograma
            data={datosGenerados.data}  // Pasamos los datos generados
            numBins={parseInt(datosGenerados.intervalos, 10)}  // Pasamos la cantidad de intervalos
          />
          <TablaFrecuencias data={datosGenerados.data}  // Pasamos los datos generados
            numBins={parseInt(datosGenerados.intervalos, 10)}  // Pasamos la cantidad de intervalos
          />
          <MostrarSerie 
            serie={datosGenerados.data}  // Pasamos los datos generados
          />
        </>
      ) : null} {/* Eliminamos el <p>Cargando datos...</p> */}
    </div>
  );
}

export default App;