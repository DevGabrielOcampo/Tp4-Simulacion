import React, { useState, useMemo, useEffect } from 'react';
import './tabla.css';
import UltimaIteracionTabla from './UltimaIteracionTabla'; // Import the new component

function TablaSimulacion({ data }) {
    const numMaquinas = 5; // Assuming fixed number of machines

    // --- ROW PAGINATION STATE ---
    const [currentPage, setCurrentPage] = useState(1);
    const iterationsPerPage = 20; // You want 20 lines per page

    // --- HELPERS FOR RENDERING ---
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

    // Helper function to get the machine an alumno is assigned to in a specific row.
    const getAlumnoMachineInRow = (alumnoId, rowData) => {
        for (let j = 1; j <= numMaquinas; j++) {
            if (rowData[`Alumno M${j}`] === alumnoId) {
                return j;
            }
        }
        return null;
    };

    // --- PROCESSED DATA (Entire Dataset) ---
    // This part remains largely the same, processing the full `data` prop.
    const processedData = useMemo(() => {
        if (!Array.isArray(data) || data.length === 0) {
            return [];
        }

        const keysToPropagate = [
            'Próxima Llegada',
            'Próximo Inicio Mantenimiento',
            'Acum. Tiempo Trabajado Tec.',
            'Tiempo Ocioso Tec.',
            'Cola',
            'Fin Mantenimiento',
            'Máquina Mant.',
        ];

        for (let i = 1; i <= numMaquinas; i++) {
            keysToPropagate.push(`Máquina ${i}`);
            keysToPropagate.push(`Fin Inscripción M${i}`);
        }

        // Collect all unique alumno IDs from the ENTIRE dataset for processing logic
        let allUniqueAlumnoIdsOverall = new Set();
        data.forEach(row => {
            for (const key in row) {
                if (key.startsWith("Estado A")) {
                    allUniqueAlumnoIdsOverall.add(key.replace("Estado ", ""));
                }
            }
        });
        const sortedAllUniqueAlumnoIdsOverall = Array.from(allUniqueAlumnoIdsOverall).sort((a, b) => {
            const numA = parseInt(a.replace('A', ''), 10);
            const numB = parseInt(b.replace('A', ''), 10);
            return numA - numB;
        });

        return data.reduce((acc, filaOriginal, index) => {
            const prevProcessedRow = acc.length > 0 ? acc[acc.length - 1] : {};
            const currentRowState = { ...filaOriginal };

            keysToPropagate.forEach(key => {
                if ((filaOriginal[key] === undefined || filaOriginal[key] === null || filaOriginal[key] === "") &&
                    (prevProcessedRow[key] !== undefined && prevProcessedRow[key] !== null && prevProcessedRow[key] !== "")) {
                    currentRowState[key] = prevProcessedRow[key];
                }
            });

            // Iterate over all unique alumno IDs from the entire dataset for propagation
            sortedAllUniqueAlumnoIdsOverall.forEach(alumnoId => {
                const estadoKey = `Estado ${alumnoId}`;
                const estadoEnFilaOriginal = filaOriginal[estadoKey];
                const maquinaAsignadaEnFilaOriginal = getAlumnoMachineInRow(alumnoId, filaOriginal);

                if (estadoEnFilaOriginal !== undefined && estadoEnFilaOriginal !== null && estadoEnFilaOriginal !== "" && estadoEnFilaOriginal !== 'FS') {
                    currentRowState[estadoKey] = estadoEnFilaOriginal;
                }
                else if ((filaOriginal.Evento && filaOriginal.Evento.includes("Fin Inscripción") &&
                    filaOriginal[`Alumno M${filaOriginal['Máquina']}`] === alumnoId) ||
                    estadoEnFilaOriginal === 'FS') {
                    currentRowState[estadoKey] = null;
                }
                else if (maquinaAsignadaEnFilaOriginal === null) {
                    currentRowState[estadoKey] = null;
                }
                else {
                    const prevEstado = prevProcessedRow[estadoKey];
                    if (prevEstado !== undefined && prevEstado !== null && prevEstado !== "" && prevEstado !== 'FS') {
                        currentRowState[estadoKey] = prevEstado;
                    } else {
                        currentRowState[estadoKey] = null;
                    }
                }
            });
            acc.push(currentRowState);
            return acc;
        }, []);
    }, [data, numMaquinas, getAlumnoMachineInRow]); // getAlumnoMachineInRow is stable if numMaquinas is stable

    // --- ROW PAGINATION LOGIC ---
    const totalIterations = processedData.length;
    const totalPages = Math.ceil(totalIterations / iterationsPerPage);

    // Get the iterations for the current page
    const indexOfLastIteration = currentPage * iterationsPerPage;
    const indexOfFirstIteration = indexOfLastIteration - iterationsPerPage;
    const currentDisplayedIterations = processedData.slice(indexOfFirstIteration, indexOfLastIteration);

    // --- DYNAMIC ALUMNO COLUMNS FOR THE CURRENT PAGE OF ITERATIONS ---
    const currentDisplayedAlumnoIds = useMemo(() => {
        let idsInCurrentPage = new Set();
        currentDisplayedIterations.forEach(row => {
            for (const key in row) {
                // Check for "Estado A#" keys AND if they are not null/empty/undefined
                if (key.startsWith("Estado A") && row[key] !== null && row[key] !== undefined && row[key] !== "" && row[key] !== 'FS') {
                    idsInCurrentPage.add(key.replace("Estado ", ""));
                }
                // Also check for "Alumno M#" keys if a student is assigned to a machine
                // (though getAlumnoMachineInRow already handles this for cells, we need to identify the column)
                // We primarily use Estado A# to identify active students for columns.
            }
        });
        // Sort for consistent column order
        return Array.from(idsInCurrentPage).sort((a, b) => {
            const numA = parseInt(a.replace('A', ''), 10);
            const numB = parseInt(b.replace('A', ''), 10);
            return numA - numB;
        });
    }, [currentDisplayedIterations]); // Re-calculate when the displayed iterations change

    // --- EFFECT TO RESET PAGE IF DATA CHANGES OR TOTAL PAGES BECOMES INVALID ---
    useEffect(() => {
        if (currentPage > totalPages && totalPages > 0) {
            setCurrentPage(totalPages); // Go to the last valid page
        } else if (currentPage === 0 && totalPages > 0) {
            setCurrentPage(1); // Ensure it's not page 0
        }
        // If data changes and totalPages becomes 0 (no data), current page will remain 1
        // and the conditional rendering for the table body will handle it.
    }, [currentPage, totalPages]);


    // --- EARLY RETURN FOR NO DATA ---
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    // Get the last iteration data for the new table
    const lastIteration = processedData.length > 0 ? processedData[processedData.length - 1] : null;
    const originalLastIterationData = data.length > 0 ? data[data.length - 1] : null;


    return (
        <>
            <div className="tabla-container">
                <h2>Estado de la Simulación</h2>
                {/* ROW PAGINATION CONTROLS */}
                {totalPages > 1 && (
                    <div className="pagination-controls">
                        <button
                            onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                            disabled={currentPage === 1}
                        >
                            Anterior Página
                        </button>
                        <span>Página {currentPage} de {totalPages}</span>
                        <button
                            onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                            disabled={currentPage === totalPages}
                        >
                            Siguiente Página
                        </button>
                    </div>
                )}
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
                                {/* Renderizar solo los encabezados de los alumnos activos en la página actual */}
                                {currentDisplayedAlumnoIds.map(alumnoId => (
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
                                {/* Renderizar solo los sub-encabezados de los alumnos activos en la página actual */}
                                {currentDisplayedAlumnoIds.map(alumnoId => (
                                    <React.Fragment key={`alumno-subheader-${alumnoId}`}>
                                        <th>Estado</th>
                                        <th>Máquina</th>
                                    </React.Fragment>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {currentDisplayedIterations.map((fila, index) => { // Iterate over currentDisplayedIterations
                                const isLlegadaEvent = fila.Evento.startsWith("Llegada Alumno");
                                const originalDataRow = data[indexOfFirstIteration + index]; // Access original row for non-propagated values
                                const rndLlegada = isLlegadaEvent ? originalDataRow?.['RND Llegada'] : null;
                                const tiempoLlegada = isLlegadaEvent ? originalDataRow?.['Tiempo Llegada'] : null;

                                const getInscripcionRND = (machineId) => {
                                    if ((originalDataRow?.Evento?.includes("Llegada Alumno") || originalDataRow?.Evento?.includes("Fin Inscripción")) && originalDataRow?.['Máquina'] === machineId) {
                                        return originalDataRow?.['RND Inscripción'];
                                    }
                                    return null;
                                };

                                const getInscripcionTiempo = (machineId) => {
                                    if ((originalDataRow?.Evento?.includes("Llegada Alumno") || originalDataRow?.Evento?.includes("Fin Inscripción")) && originalDataRow?.['Máquina'] === machineId) {
                                        return originalDataRow?.['Tiempo Inscripción'];
                                    }
                                    return null;
                                };

                                return (
                                    <tr key={`fila-${indexOfFirstIteration + index}`}> {/* Use a unique key based on global index */}
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

                                        <td>{renderValue(originalDataRow?.['RND Tiempo Vuelta'])}</td>
                                        <td>{renderValue(originalDataRow?.['Tiempo Vuelta'])}</td>
                                        <td>
                                            {fila['Próximo Inicio Mantenimiento'] <= fila.Reloj
                                                ? '-'
                                                : renderValue(fila['Próximo Inicio Mantenimiento'])}
                                        </td>

                                        <td>{renderValue(originalDataRow?.['RND Mantenimiento'])}</td>
                                        <td>{renderValue(originalDataRow?.['Tiempo Mantenimiento'])}</td>

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

                                        {/* ALUMNOS - Solo renderiza las celdas de los alumnos que están activos en esta página de iteraciones */}
                                        {currentDisplayedAlumnoIds.map(alumnoId => {
                                            const estado = fila[`Estado ${alumnoId}`]; // Estado ya procesado
                                            const maquina = getAlumnoMachineInRow(originalDataRow? alumnoId : null, originalDataRow); // Máquina de la fila ORIGINAL

                                            const isAlumnoActiveInCurrentRow = (estado !== null && estado !== undefined && estado !== "") || (maquina !== null);

                                            return (
                                                <React.Fragment key={`estado-maquina-alumno-${alumnoId}`}>
                                                    <td>{isAlumnoActiveInCurrentRow ? renderValue(estado) : '-'}</td>
                                                    <td>{isAlumnoActiveInCurrentRow ? renderIntValue(maquina) : '-'}</td>
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
            {/* Render the new table for the last iteration */}
            {lastIteration && (
                <UltimaIteracionTabla
                    lastIterationData={lastIteration}
                    numMaquinas={numMaquinas}
                />
            )}
        </>
    );
}

export default TablaSimulacion;