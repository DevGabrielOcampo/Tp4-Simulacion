import React from "react";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import Datos from './components/Datos/Datos';
import Histograma from "./components/Histograma/Histograma";


function App() {
  return (
    <div className="App m-4">
      <Datos /> 
      <div className="d-flex justify-content-center mt-4"> 
        <Histograma />
      </div>
    </div>
  );
}

export default App;
