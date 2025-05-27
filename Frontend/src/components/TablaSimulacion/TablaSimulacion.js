import React from 'react';
import './tabla.css'; // Asumiendo que tu archivo CSS sigue siendo el mismo

function TablaSimulacion({ data }) {
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    const primeraIteracion = data[0];
    const numMaquinas = 5;

    // Determinar dinámicamente maxAlumnos basado en el campo 'max_alumnos' de los datos
    // Esto hace que la tabla sea adaptable si el número de alumnos cambia en el futuro
    let maxAlumnos = 0;
    if (primeraIteracion && typeof primeraIteracion.max_alumnos === 'number') {
        maxAlumnos = primeraIteracion.max_alumnos;
    }

    // Función auxiliar para renderizar valores, manejando null/undefined/vacío y formato de números
    const renderValue = (value) => {
        // Tratar null, undefined y string vacío como "-"
        if (value === null || value === undefined || value === "") {
            return "-";
        }
        // Si el valor es un número, formatearlo a 2 decimales
        if (typeof value === 'number') {
            return value.toFixed(2);
        }
        // De lo contrario, devolver el valor tal cual
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
                            <th colSpan="4">DETALLE MANTENIMIENTO</th> {/* Colspan ajustado para solo mostrar datos relevantes del evento actual */}
                            <th colSpan="4">ESTADÍSTICAS TÉCNICO</th> {/* NUEVO: Encabezado para estadísticas del técnico */}
                            <th rowSpan="2">ACUM. ABANDONOS</th>
                            <th rowSpan="2">COLA DE ALUMNOS</th>

                            {[...Array(numMaquinas)].map((_, i) => (
                                <th key={`maquina-estado-header-${i + 1}`} rowSpan="2">Estado Máquina {i + 1}</th>
                            ))}

                            {/* Encabezados de Alumnos dinámicos basados en maxAlumnos */}
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
                            <th>Próximo Inicio Mantenimiento</th> {/* Actualizado de "Vuelta Tecnico" para mayor claridad */}

                            {/* DETALLE MANTENIMIENTO: Estos se mostrarán solo si el evento actual los genera */}
                            <th>RND Mant.</th>
                            <th>Tiempo Mant.</th>
                            <th>Fin Mant.</th>
                            <th>Máq. Mant.</th>


                            {/* NUEVO: Subencabezados para las estadísticas del técnico */}
                            <th>Tiempo Ocioso Tec.</th>
                            <th>Promedio Tiempo Ocioso Tec.</th>
                            <th>Acum. Tiempo Trabajado Tec.</th>
                            <th>Promedio Tiempo Trabajado Tec.</th>


                            {/* Ya no hay "Tiempo Espera Acumulado" ni "Tiempo Espera Promedio" en el encabezado */}

                            {[...Array(maxAlumnos)].map((_, i) => (
                                <React.Fragment key={`alumno-subheader-${i + 1}`}>
                                    <th>Estado</th>
                                    {/* Eliminado el subencabezado de Tiempo Espera individual */}
                                </React.Fragment>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((fila, index) => {
                            // Función auxiliar para obtener un valor, si no está en la fila actual,
                            // busca en la fila anterior para los valores que se propagan.
                            const getVal = (key) => {
                                // Si la fila actual tiene el valor y no es nulo/indefinido, usarlo.
                                if (fila[key] !== undefined && fila[key] !== null) {
                                    return fila[key];
                                }
                                // Si no, intentar obtenerlo de la fila anterior (para valores que se propagan)
                                if (index > 0 && data[index - 1][key] !== undefined && data[index - 1][key] !== null) {
                                    return data[index - 1][key];
                                }
                                return null; // Devolver null para que renderValue lo convierta en "-"
                            };

                            // Lógica específica para RND y Tiempo de Llegada (solo en eventos de "Llegada Alumno")
                            const isLlegadaEvent = fila.Evento.startsWith("Llegada Alumno");
                            const rndLlegada = isLlegadaEvent ? renderValue(fila['RND Llegada']) : "-";
                            const tiempoLlegada = isLlegadaEvent ? renderValue(fila['Tiempo Llegada']) : "-";

                            // Estas funciones ahora obtienen correctamente los datos del evento actual si es relevante para esa máquina.
                            // Si no, devuelven "-" para evitar propagar RNDs/Tiempos irrelevantes.
                            const getInscripcionRND = (machineId) => {
                                // Comprueba si el evento actual es una "Llegada Alumno" o "Fin Inscripción"
                                // Y si la 'Máquina' involucrada en el evento coincide con el 'machineId' actual.
                                if ((fila.Evento.startsWith("Llegada Alumno") || fila.Evento.startsWith("Fin Inscripción")) && fila['Máquina'] === machineId) {
                                    return renderValue(fila['RND Inscripción']);
                                }
                                return "-";
                            };

                            const getInscripcionTiempo = (machineId) => {
                                if ((fila.Evento.startsWith("Llegada Alumno") || fila.Evento.startsWith("Fin Inscripción")) && fila['Máquina'] === machineId) {
                                    return renderValue(fila['Tiempo Inscripción']);
                                }
                                return "-";
                            };

                            return (
                                <tr key={`fila-${index}`}>
                                    <td>{fila.Iteracion}</td>
                                    <td>{fila.Evento}</td>
                                    <td>{renderValue(fila.Reloj)}</td>

                                    <td>{rndLlegada}</td>
                                    <td>{tiempoLlegada}</td>
                                    <td>{renderValue(getVal('Próxima Llegada'))}</td>

                                    {[...Array(numMaquinas)].map((_, i) => {
                                        const currentMachineId = i + 1;
                                        const finInscripcionKey = `Fin Inscripción M${currentMachineId}`;

                                        return (
                                            <React.Fragment key={`maquina-inscripcion-data-${currentMachineId}`}>
                                                <td>{getInscripcionRND(currentMachineId)}</td>
                                                <td>{getInscripcionTiempo(currentMachineId)}</td>
                                                <td>{renderValue(getVal(finInscripcionKey))}</td>
                                            </React.Fragment>
                                        );
                                    })}

                                    <td>{renderValue(getVal('RND Tiempo Vuelta'))}</td>
                                    <td>{renderValue(getVal('Tiempo Vuelta'))}</td>
                                    <td>{renderValue(getVal('Próximo Inicio Mantenimiento'))}</td>

                                    {/* DETALLE MANTENIMIENTO - Solo se muestra si el evento es relevante para el mantenimiento */}
                                    <td>{fila.Evento.startsWith("Fin Mantenimiento") || fila.Evento === "Inicializacion" || fila.Evento === "Regreso Técnico" ? renderValue(fila['RND Mantenimiento']) : "-"}</td>
                                    <td>{fila.Evento.startsWith("Fin Mantenimiento") || fila.Evento === "Inicializacion" || fila.Evento === "Regreso Técnico" ? renderValue(fila['Tiempo Mantenimiento']) : "-"}</td>
                                    <td>{fila.Evento.startsWith("Fin Mantenimiento") || fila.Evento === "Inicializacion" || fila.Evento === "Regreso Técnico" ? renderValue(fila['Fin Mantenimiento']) : "-"}</td>
                                    {/* <td>{fila.Evento.startsWith("Fin Mantenimiento") || fila.Evento === "Inicializacion" || fila.Evento === "Regreso Técnico" ? renderValue(fila['Máquina Mant.']) : "-"}</td> */}
                                    <td>{
                                        (fila.Evento.startsWith("Fin Mantenimiento") || fila.Evento === "Inicializacion" || fila.Evento === "Regreso Técnico")
                                            ? parseInt(renderValue(fila['Máquina Mant.']), 10)
                                            : "-"
                                    }</td>

                                    {/* ESTADÍSTICAS TÉCNICO - Estos son acumulativos y se propagan */}
                                    <td>{renderValue(getVal('Tiempo Ocioso Tec.'))}</td>
                                    <td>{renderValue(getVal('Promedio Tiempo Ocioso Tec.'))}</td>
                                    <td>{renderValue(getVal('Acum. Tiempo Trabajado Tec.'))}</td>
                                    <td>{renderValue(getVal('Promedio Tiempo Trabajado Tec.'))}</td>

                                    <td>{parseInt(getVal('acumAbandonos'))}</td>
                                    <td>{parseInt(getVal('Cola'))}</td>
                                    {/* Eliminado: Tiempo Espera Acumulado y Promedio de Alumnos */}

                                    {[...Array(numMaquinas)].map((_, i) => (
                                        <td key={`maquina-estado-body-${i + 1}`}>
                                            {renderValue(getVal(`Máquina ${i + 1}`))}
                                        </td>
                                    ))}

                                    {[...Array(maxAlumnos)].map((_, i) => {
                                        const alumnoId = `A${i + 1}`;
                                        return (
                                            <React.Fragment key={`alumno-data-${alumnoId}`}>
                                                <td>{renderValue(getVal(`Estado ${alumnoId}`))}</td>
                                                {/* Eliminado: Tiempo Espera individual del alumno */}
                                            </React.Fragment>
                                        );
                                    })}
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