import React from 'react';
import './tabla.css';

function TablaSimulacion({ data }) {
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    const numMaquinas = 5;

    // Determinar maxAlumnos dinámicamente desde todo el conjunto de datos
    let maxAlumnos = 0;
    data.forEach(row => {
        if (row && typeof row.max_alumnos === 'number') {
            maxAlumnos = Math.max(maxAlumnos, row.max_alumnos);
        }
    });

    // Función auxiliar para renderizar valores, manejando null/undefined/vacío y formato de números
    const renderValue = (value) => {
        if (value === null || value === undefined || value === "") {
            return "-";
        }
        if (typeof value === 'number') {
            return value.toFixed(2);
        }
        return value;
    };

    return (
        <div className="tabla-container">
            <h2>Estado de la Simulación</h2>
            <div className="tabla-wrapper">
                <table border="1" className="tabla-simulacion">
                    <thead>
                        <tr>
                            <th rowSpan="2">N° FILA</th>
                            <th rowSpan="2">EVENTO</th>
                            <th rowSpan="2">RELOJ (MINUTOS)</th>
                            <th colSpan="3">LLEGADA ALUMNO</th>

                            {[...Array(numMaquinas)].map((_, i) => (
                                <th key={`maquina-inscripcion-header-${i + 1}`} colSpan="3">Máquina {i + 1} (Inscripción)</th>
                            ))}

                            <th colSpan="3">REGRESO TÉCNICO</th>
                            <th colSpan="4">DETALLE MANTENIMIENTO</th>
                            <th colSpan="4">ESTADÍSTICAS TÉCNICO</th>

                            <th rowSpan="2">COLA DE ALUMNOS</th>

                            {[...Array(numMaquinas)].map((_, i) => (
                                <th key={`maquina-estado-header-${i + 1}`} rowSpan="2">Estado Máquina {i + 1}</th>
                            ))}

                            {[...Array(maxAlumnos)].map((_, i) => (
                                <th key={`alumno-header-${i + 1}`} colSpan="1">
                                    Alumno A{i + 1}
                                </th>
                            ))}
                        </tr>
                        <tr>
                            <th>RND</th>
                            <th>Tiempo Llegada</th>
                            <th>Próxima Llegada</th>

                            {[...Array(numMaquinas)].map((_, i) => (
                                <React.Fragment key={`maquina-inscripcion-subheader-${i + 1}`}>
                                    <th>RND Inscripción</th>
                                    <th>Tiempo Inscripción</th>
                                    <th>Fin Inscripción</th>
                                </React.Fragment>
                            ))}

                            <th>RND Regreso</th>
                            <th>Tiempo Descanso</th>
                            <th>Próximo Inicio Mantenimiento</th>

                            <th>RND Mant.</th>
                            <th>Tiempo Mant.</th>
                            <th>Fin Mant.</th>
                            <th>Máq. Mant.</th>

                            <th>Tiempo Ocioso Tec.</th>
                            <th>Promedio Tiempo Ocioso Tec.</th>
                            <th>Acum. Tiempo Trabajado Tec.</th>
                            <th>Promedio Tiempo Trabajado Tec.</th>

                            {[...Array(maxAlumnos)].map((_, i) => (
                                <React.Fragment key={`alumno-subheader-${i + 1}`}>
                                    <th>Estado</th>
                                </React.Fragment>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
  {data.map((fila, index) => {
    const getPropagatedValue = (key) => {
      if (fila[key] !== undefined && fila[key] !== null && fila[key] !== "") {
        return fila[key];
      }
      if (index > 0 && data[index - 1][key] !== undefined && data[index - 1][key] !== null) {
        return data[index - 1][key];
      }
      return null;
    };

    const renderValue = (value) => {
      if (value === null || value === undefined || value === "") {
        return "-";
      }
      if (typeof value === 'number') {
        return value.toFixed(2);
      }
      return value;
    };

    const numMaquinas = 5;

    // Calcular el fin de mantenimiento mínimo y la máquina correspondiente para esta fila
    let minFinMant = null;
    let maquinaMant = null;
    for (let j = 1; j <= numMaquinas; j++) {
      const finMant = fila[`Fin Mantenimiento M${j}`];
      if (finMant !== null && finMant !== undefined) {
        if (minFinMant === null || finMant < minFinMant) {
          minFinMant = finMant;
          maquinaMant = j;
        }
      }
    }

    return (
      <tr key={`fila-${index}`}>
        <td>{index + 1}</td>
        <td>{fila.Evento}</td>
        <td>{renderValue(fila.Reloj)}</td>

        <td>{renderValue(fila['RND Llegada'])}</td>
        <td>{renderValue(fila['Tiempo Llegada'])}</td>
        <td>{renderValue(getPropagatedValue('Próxima Llegada'))}</td>

        {[...Array(numMaquinas)].map((_, i) => {
          const machineId = i + 1;
          return (
            <React.Fragment key={`inscripcion-${machineId}`}>
              <td>{renderValue(fila['RND Inscripción'])}</td>
              <td>{renderValue(fila['Tiempo Inscripción'])}</td>
              <td>{renderValue(fila[`Fin Inscripción M${machineId}`])}</td>
            </React.Fragment>
          );
        })}

        <td>{renderValue(fila['RND Tiempo Vuelta'])}</td>
        <td>{renderValue(fila['Tiempo Vuelta'])}</td>
        <td>{renderValue(getPropagatedValue('Próximo Inicio Mantenimiento'))}</td>

        <td>{renderValue(fila['RND Mantenimiento'])}</td>
        <td>{renderValue(fila['Tiempo Mantenimiento'])}</td>

        {/* Ahora con las variables calculadas */}
        <td>{renderValue(minFinMant)}</td>
        <td>{renderValue(maquinaMant)}</td>

        <td>{renderValue(getPropagatedValue('Tiempo Ocioso Tec.'))}</td>
        <td>{renderValue(getPropagatedValue('Promedio Tiempo Ocioso Tec.'))}</td>
        <td>{renderValue(getPropagatedValue('Acum. Tiempo Trabajado Tec.'))}</td>
        <td>{renderValue(getPropagatedValue('Promedio Tiempo Trabajado Tec.'))}</td>

        <td>{renderValue(getPropagatedValue('Cola'))}</td>

        {[...Array(numMaquinas)].map((_, i) => (
          <td key={`estado-maquina-${i + 1}`}>{renderValue(getPropagatedValue(`Máquina ${i + 1}`))}</td>
        ))}

        {[...Array(maxAlumnos)].map((_, i) => (
          <td key={`estado-alumno-${i + 1}`}>{renderValue(getPropagatedValue(`Estado A${i + 1}`))}</td>
        ))}
      </tr>
    );
  })}
</tbody>

                </table>
            </div>
        </div>
    );
}

export default TablaSimulacion;
