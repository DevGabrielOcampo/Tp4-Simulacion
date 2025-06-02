import React from 'react';
import './tabla.css';

function TablaSimulacion({ data }) {
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    const numMaquinas = 5;

    // Recopilar y ordenar todos los IDs de alumno
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

    // Keys que deberían propagarse a lo largo de las filas
    const keysToPropagate = [
        'Próxima Llegada',
        'Próximo Inicio Mantenimiento',
        'Acum. Tiempo Trabajado Tec.',
        'Tiempo Ocioso Tec.',
        'Cola',
        'Fin Mantenimiento',
        'Máquina Mant.',
    ];

    // Aseguramos que los estados de las máquinas se propaguen
    for (let i = 1; i <= numMaquinas; i++) {
        keysToPropagate.push(`Máquina ${i}`); // Estado de la máquina (Ej: "Libre", "Ocupado")
        keysToPropagate.push(`Fin Inscripción M${i}`); // Tiempo de fin de inscripción de esa máquina
    }

    // Los estados de los alumnos NO SE INCLUYEN AQUÍ para propagación general,
    // ya que su lógica es muy específica y se maneja por separado para un reseteo más estricto.
    // sortedAllAlumnoIds.forEach(alumnoId => {
    //     keysToPropagate.push(`Estado ${alumnoId}`);
    // });

    const renderValue = (value) => {
        if (value === null || value === undefined || value === "" || (typeof value === 'number' && isNaN(value))) {
            return "-";
        }
        if (typeof value === 'number') {
            return value.toFixed(2);
        }
        return value;
    };

    const renderIntValue = (value) => {
        if (value === null || value === undefined || value === "" || (typeof value === 'number' && isNaN(value))) {
            return "-";
        }
        if (typeof value === 'number') {
            return Math.floor(value);
        }
        return value;
    };

    // Función para obtener la máquina a la que un alumno está asignado en una fila específica.
    const getAlumnoMachineInRow = (alumnoId, rowData) => {
        for (let j = 1; j <= numMaquinas; j++) {
            if (rowData[`Alumno M${j}`] === alumnoId) {
                return j;
            }
        }
        return null;
    };

    // --- CONSTRUCCIÓN DEL ESTADO PROCESADO UTILIZANDO REDUCE ---
    const processedData = data.reduce((acc, filaOriginal, index) => {
        const prevProcessedRow = acc.length > 0 ? acc[acc.length - 1] : {};
        const currentRowState = { ...filaOriginal }; // Copia inicial de la fila original

        // 1. Lógica general de propagación para todos los 'keysToPropagate'
        keysToPropagate.forEach(key => {
            if ((filaOriginal[key] === undefined || filaOriginal[key] === null || filaOriginal[key] === "") &&
                (prevProcessedRow[key] !== undefined && prevProcessedRow[key] !== null && prevProcessedRow[key] !== "")) {
                currentRowState[key] = prevProcessedRow[key];
            }
        });

        // 2. Lógica ESPECÍFICA y estricta para el estado del alumno
        sortedAllAlumnoIds.forEach(alumnoId => {
            const estadoKey = `Estado ${alumnoId}`;
            const estadoEnFilaOriginal = filaOriginal[estadoKey];
            const maquinaAsignadaEnFilaOriginal = getAlumnoMachineInRow(alumnoId, filaOriginal); // ¿Está en una máquina en esta fila original?

            // **Nuevo enfoque para el estado del alumno:**
            // Si el estado está explícitamente en la fila original (y no es 'FS'), ese tiene prioridad.
            if (estadoEnFilaOriginal !== undefined && estadoEnFilaOriginal !== null && estadoEnFilaOriginal !== "" && estadoEnFilaOriginal !== 'FS') {
                currentRowState[estadoKey] = estadoEnFilaOriginal;
            }
            // Si el evento actual es "Fin Inscripción" y es para este alumno,
            // o si el estado en la fila original es 'FS', entonces el alumno YA NO ESTÁ.
            else if ((filaOriginal.Evento && filaOriginal.Evento.includes("Fin Inscripción") &&
                filaOriginal[`Alumno M${filaOriginal['Máquina']}`] === alumnoId) ||
                estadoEnFilaOriginal === 'FS') {
                currentRowState[estadoKey] = null; // Fuerza a null (para que se muestre '-')
            }
            // Si no está en la fila original, y NO está asignado a una máquina en la fila original,
            // entonces su estado DEBE ser null, sin importar la propagación anterior.
            else if (maquinaAsignadaEnFilaOriginal === null) {
                currentRowState[estadoKey] = null; // Si no hay máquina, el estado se resetea.
            }
            // En cualquier otro caso (no está en fila original, no es evento de salida, y SÍ tiene máquina asignada),
            // se propaga el estado de la fila procesada anterior.
            else {
                const prevEstado = prevProcessedRow[estadoKey];
                if (prevEstado !== undefined && prevEstado !== null && prevEstado !== "" && prevEstado !== 'FS') {
                    currentRowState[estadoKey] = prevEstado;
                } else {
                    currentRowState[estadoKey] = null; // Por si acaso no se encuentra un estado válido para propagar
                }
            }
        });

        acc.push(currentRowState); // Agregar la fila procesada al acumulador
        return acc;
    }, []);

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
                        {processedData.map((fila, index) => {
                            const isLlegadaEvent = fila.Evento.startsWith("Llegada Alumno");
                            const rndLlegada = isLlegadaEvent ? data[index]['RND Llegada'] : null;
                            const tiempoLlegada = isLlegadaEvent ? data[index]['Tiempo Llegada'] : null;

                            const getInscripcionRND = (machineId) => {
                                if ((data[index].Evento.includes("Llegada Alumno") || data[index].Evento.includes("Fin Inscripción")) && data[index]['Máquina'] === machineId) {
                                    return data[index]['RND Inscripción'];
                                }
                                return null;
                            };

                            const getInscripcionTiempo = (machineId) => {
                                if ((data[index].Evento.includes("Llegada Alumno") || data[index].Evento.includes("Fin Inscripción")) && data[index]['Máquina'] === machineId) {
                                    return data[index]['Tiempo Inscripción'];
                                }
                                return null;
                            };

                            return (
                                <tr key={`fila-${index}`}>
                                    <td>{renderIntValue(fila['Iteracion'])}</td>
                                    <td>{fila.Evento}</td>
                                    <td>{renderValue(fila.Reloj)}</td>
                                    <td>{renderValue(rndLlegada)}</td>
                                    <td>{renderValue(tiempoLlegada)}</td>
                                    <td>{renderValue(fila['Próxima Llegada'])}</td>

                                    {[...Array(numMaquinas)].map((_, i) => {
                                        const machineId = i + 1;
                                        return (
                                            <React.Fragment key={`inscripcion-${machineId}`}>
                                                <td>{renderValue(getInscripcionRND(machineId))}</td>
                                                <td>{renderValue(getInscripcionTiempo(machineId))}</td>
                                                <td>
                                                    {fila[`Fin Inscripción M${machineId}`] <= fila.Reloj
                                                        ? '-'
                                                        : renderValue(fila[`Fin Inscripción M${machineId}`])}
                                                </td>

                                            </React.Fragment>
                                        );
                                    })}

                                    <td>{renderValue(data[index]['RND Tiempo Vuelta'])}</td>
                                    <td>{renderValue(data[index]['Tiempo Vuelta'])}</td>
                                    <td>
                                        {fila['Próximo Inicio Mantenimiento'] <= fila.Reloj
                                            ? '-'
                                            : renderValue(fila['Próximo Inicio Mantenimiento'])}
                                    </td>


                                    <td>{renderValue(data[index]['RND Mantenimiento'])}</td>
                                    <td>{renderValue(data[index]['Tiempo Mantenimiento'])}</td>
                                    
                                    <td>
                                        {fila['Fin Mantenimiento'] <= fila.Reloj
                                            ? '-'
                                            : renderValue(fila['Fin Mantenimiento'])}
                                    </td>
                                    <td>{renderIntValue(fila['Máquina Mant.'])}</td>

                                    <td>{renderValue(fila['Acum. Tiempo Trabajado Tec.'])}</td>
                                    <td>{renderValue(fila['Tiempo Ocioso Tec.'])}</td>
                                    <td>
                                        {renderValue(
                                            (parseFloat(fila['Tiempo Ocioso Tec.']) || 0) /
                                            ((parseFloat(fila['Acum. Tiempo Trabajado Tec.']) || 0) + (parseFloat(fila['Tiempo Ocioso Tec.']) || 0) || 1)
                                        )}
                                    </td>
                                    <td>
                                        {renderValue(
                                            ((parseFloat(fila['Tiempo Ocioso Tec.']) || 0) * 100) /
                                            ((parseFloat(fila['Acum. Tiempo Trabajado Tec.']) || 0) + (parseFloat(fila['Tiempo Ocioso Tec.']) || 0) || 1)
                                        )}%
                                    </td>

                                    <td>{renderValue(fila['Cola'])}</td>

                                    {[...Array(numMaquinas)].map((_, i) => (
                                        <td key={`estado-maquina-${i + 1}`}>
                                            {renderValue(fila[`Máquina ${i + 1}`])}
                                        </td>
                                    ))}

                                    {/* ALUMNOS */}
                                    {sortedAllAlumnoIds.map(alumnoId => {
                                        const estado = fila[`Estado ${alumnoId}`]; // Estado ya procesado
                                        const maquina = getAlumnoMachineInRow(alumnoId, data[index]); // Máquina de la fila ORIGINAL

                                        // Solo mostrar si hay un estado NO NULO O una máquina asignada
                                        const isAlumnoActive = (estado !== null) || (maquina !== null);

                                        if (!isAlumnoActive) {
                                            return (
                                                <React.Fragment key={`estado-maquina-alumno-${alumnoId}`}>
                                                    <td>-</td>
                                                    <td>-</td>
                                                </React.Fragment>
                                            );
                                        }
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