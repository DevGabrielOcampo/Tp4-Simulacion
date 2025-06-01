import React from 'react';
import './tabla.css';

function TablaSimulacion({ data }) {
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    const numMaquinas = 5;

    const renderValue = (value) => {
        if (value === null || value === undefined || value === "") {
            return "-";
        }
        if (typeof value === 'number') {
            return value.toFixed(2);
        }
        return value;
    };

    const renderIntValue = (value) => {
        if (value === null || value === undefined || value === "") {
            return "-";
        }
        if (typeof value === 'number') {
            return Math.floor(value);
        }
        return value;
    };

    let allAlumnoIds = new Set();
    data.forEach(row => {
        for (const key in row) {
            if (key.startsWith("Estado A")) {
                allAlumnoIds.add(key.replace("Estado ", ""));
            }
        }
    });
    const sortedAllAlumnoIds = Array.from(allAlumnoIds).sort((a, b) => {
        const numA = parseInt(a.replace('A', ''), 10);
        const numB = parseInt(b.replace('A', ''), 10);
        return numA - numB;
    });

    return (
        <div className="tabla-container">
            <h2>Estado de la Simulación</h2>
            <div className="tabla-wrapper">
                <table border="1" className="tabla-simulacion">
                    <thead>
                        <tr>
                            <th rowSpan="2">ITERACIÓN</th>
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
                            {sortedAllAlumnoIds.map(alumnoId => (
                                <th key={`alumno-header-${alumnoId}`} colSpan="2">{alumnoId}</th>
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
                            <th>Acum. Tiempo Trabajado Tec.</th>
                            <th>Tiempo Ocioso Tec.</th>
                            <th>Prom. Tiempo Ocioso Tec.</th>
                            <th>% Tiempo Ocioso Tec.</th>
                            {sortedAllAlumnoIds.map(alumnoId => (
                                <React.Fragment key={`alumno-subheader-${alumnoId}`}>
                                    <th>Estado</th>
                                    <th>Máquina</th>
                                </React.Fragment>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((fila, index) => {
                            // Definimos explícitamente qué claves deben propagarse (mantener su valor de la fila anterior)
                            // y cuáles deben ser específicas de la fila actual (null si no están presentes).
                            const propagatedKeys = [
                                'Próxima Llegada',
                                'Próximo Inicio Mantenimiento',
                                'Acum. Tiempo Trabajado Tec.',
                                'Tiempo Ocioso Tec.',
                                'Promedio Tiempo Ocioso Tec.',
                                'Cola',
                                'Fin Mantenimiento', // Ahora propagamos el 'Fin Mantenimiento' general
                                'Máquina Mant.', // Y la 'Máquina Mant.' general
                                // Estados de máquinas se propagan si no cambian en la fila actual
                                ...Array(numMaquinas).fill(0).map((_, i) => `Máquina ${i + 1}`),
                                // También propagamos los Fin Mantenimiento específicos de cada máquina
                                ...Array(numMaquinas).fill(0).map((_, i) => `Fin Mantenimiento M${i + 1}`)
                            ];

                            // Función para obtener valores:
                            // - Si el valor existe en la fila actual, lo devuelve.
                            // - Si no existe en la fila actual, pero la clave está en `propagatedKeys`, lo busca en la fila anterior.
                            // - De lo contrario, devuelve null (renderValue lo convierte a "-").
                            const getValue = (key) => {
                                // Prioridad: valor de la fila actual
                                if (fila[key] !== undefined && fila[key] !== null && fila[key] !== "") {
                                    return fila[key];
                                }
                                // Si no está en la fila actual, y la clave es una de las que se deben propagar,
                                // busca el valor de la fila anterior.
                                if (propagatedKeys.includes(key) && index > 0) {
                                    return data[index - 1][key];
                                }
                                // Para todas las demás claves (RNDs, Tiempos de Evento, Fines de Evento, Estados de Alumno, Máquina Asignada a Alumno)
                                // NO se propagan si no están explícitamente en la fila actual.
                                return null;
                            };

                            const isLlegadaEvent = fila.Evento.startsWith("Llegada Alumno");
                            const rndLlegada = isLlegadaEvent ? fila['RND Llegada'] : null;
                            const tiempoLlegada = isLlegadaEvent ? fila['Tiempo Llegada'] : null;

                            const getInscripcionRND = (machineId) => {
                                if ((fila.Evento.includes("Llegada Alumno") || fila.Evento.includes("Fin Inscripción")) && fila['Máquina'] === machineId) {
                                    return fila['RND Inscripción'];
                                }
                                return null;
                            };

                            const getInscripcionTiempo = (machineId) => {
                                if ((fila.Evento.includes("Llegada Alumno") || fila.Evento.includes("Fin Inscripción")) && fila['Máquina'] === machineId) {
                                    return fila['Tiempo Inscripción'];
                                }
                                return null;
                            };
                            
                            // Fin Inscripción: Obtenemos el valor directamente de la fila, no se propaga
                            const getFinInscripcion = (machineId) => fila[`Fin Inscripción M${machineId}`];

                            // minFinMant y maquinaMant deben leer directamente de la fila 'Fin Mantenimiento' y 'Máquina Mant.'
                            // y estos sí deben propagarse si no cambian.
                            const finMantenimientoGeneral = getValue('Fin Mantenimiento');
                            const maquinaMantenimientoGeneral = getValue('Máquina Mant.');


                            // **Modificación clave para el estado y máquina del alumno**
                            const getAlumnoDisplayInfo = (alumnoId) => {
                                const estadoActual = fila[`Estado ${alumnoId}`];
                                let maquinaActual = null;
                                for (let j = 1; j <= numMaquinas; j++) {
                                    // Verifica si la máquina tiene un alumno asignado en esta fila y si es el alumno actual
                                    if (fila[`Alumno M${j}`] === alumnoId) {
                                        maquinaActual = j;
                                        break;
                                    }
                                }

                                if (index > 0) {
                                    const prevFila = data[index - 1];
                                    const prevEvento = prevFila.Evento;
                                    
                                    const prevFinInscripcionMatch = prevEvento.match(/Fin Inscripción Máq\. M(\d+) Alumno (A\d+)/);
                                    const alumnoTerminoInscripcionEnFilaAnterior = prevFinInscripcionMatch && prevFinInscripcionMatch[2] === alumnoId;

                                    if (alumnoTerminoInscripcionEnFilaAnterior) {
                                        // Si el alumno terminó la inscripción en la fila anterior y ahora no tiene estado o está "AF"
                                        // O si el estado en esta fila es "AF"
                                        if (estadoActual === 'AF' || estadoActual === undefined || estadoActual === null) {
                                            return { estado: "FS", maquina: null }; // Forzar a "FS" y nulo en la siguiente fila
                                        }
                                    }
                                }
                                
                                return { estado: estadoActual, maquina: maquinaActual };
                            };


                            return (
                                <tr key={`fila-${index}`}>
                                    <td>{fila['Iteracion']}</td>
                                    <td>{fila.Evento}</td>
                                    <td>{renderValue(fila.Reloj)}</td>
                                    <td>{renderValue(rndLlegada)}</td>
                                    <td>{renderValue(tiempoLlegada)}</td>
                                    <td>{renderValue(getValue('Próxima Llegada'))}</td>

                                    {[...Array(numMaquinas)].map((_, i) => {
                                        const machineId = i + 1;
                                        return (
                                            <React.Fragment key={`inscripcion-${machineId}`}>
                                                <td>{renderValue(getInscripcionRND(machineId))}</td>
                                                <td>{renderValue(getInscripcionTiempo(machineId))}</td>
                                                <td>{renderValue(getFinInscripcion(machineId))}</td>
                                            </React.Fragment>
                                        );
                                    })}

                                    <td>{renderValue(fila['RND Tiempo Vuelta'])}</td>
                                    <td>{renderValue(fila['Tiempo Vuelta'])}</td>
                                    <td>{renderValue(getValue('Próximo Inicio Mantenimiento'))}</td>

                                    {/* DETALLE MANTENIMIENTO: Se muestra RND/Tiempo Mantenimiento solo si es el evento */}
                                    <td>{renderValue(fila.Evento.includes("Inicio Mantenimiento") ? fila['RND Mantenimiento'] : null)}</td>
                                    <td>{renderValue(fila.Evento.includes("Inicio Mantenimiento") ? fila['Tiempo Mantenimiento'] : null)}</td>
                                    <td>{renderValue(finMantenimientoGeneral)}</td> {/* Usa el valor propagado */}
                                    <td>{renderIntValue(maquinaMantenimientoGeneral)}</td> {/* Usa el valor propagado */}

                                    <td>{renderValue(getValue('Acum. Tiempo Trabajado Tec.'))}</td>
                                    <td>{renderValue(getValue('Tiempo Ocioso Tec.'))}</td>
                                    {/* Para el promedio y porcentaje, si el tiempo total es 0, evitamos división por 0 */}
                                    <td>{renderValue((parseFloat(getValue('Tiempo Ocioso Tec.')) || 0) / ((parseFloat(getValue('Acum. Tiempo Trabajado Tec.')) || 0) + (parseFloat(getValue('Tiempo Ocioso Tec.')) || 0) || 1))}</td>
                                    <td>{renderValue(((parseFloat(getValue('Tiempo Ocioso Tec.')) || 0) * 100 / ((parseFloat(getValue('Acum. Tiempo Trabajado Tec.')) || 0) + (parseFloat(getValue('Tiempo Ocioso Tec.')) || 0) || 1)))}%</td>

                                    <td>{renderValue(getValue('Cola'))}</td>

                                    {[...Array(numMaquinas)].map((_, i) => (
                                        <td key={`estado-maquina-${i + 1}`}>{renderValue(getValue(`Máquina ${i + 1}`))}</td>
                                    ))}

                                    {sortedAllAlumnoIds.map(alumnoId => {
                                        const { estado, maquina } = getAlumnoDisplayInfo(alumnoId);
                                        
                                        return (
                                            <React.Fragment key={`estado-maquina-alumno-${alumnoId}`}>
                                                <td>{renderValue(estado)}</td>
                                                <td>{renderIntValue(maquina)}</td>
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