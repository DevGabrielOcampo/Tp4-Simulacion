import React, { useState, useMemo, useEffect } from 'react';
import './tabla.css';
import UltimaIteracionTabla from './UltimaIteracionTabla'; // Import the new component

const variable = 1500; // No tocar

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

    // --- PROCESSED DATA (Entire Dataset, but limited for display) ---
    const processedData = useMemo(() => {
        if (!Array.isArray(data) || data.length === 0) {
            return [];
        }

        const limitedData = data.slice(0, variable);

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

        // Collect all unique alumno IDs from the LIMITED dataset for processing logic
        let allUniqueAlumnoIdsOverall = new Set();
        limitedData.forEach(row => {
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

        return limitedData.reduce((acc, filaOriginal, index) => {
            const prevProcessedRow = acc.length > 0 ? acc[acc.length - 1] : {};
            const currentRowState = { ...filaOriginal };

            keysToPropagate.forEach(key => {
                if ((filaOriginal[key] === undefined || filaOriginal[key] === null || filaOriginal[key] === "") &&
                    (prevProcessedRow[key] !== undefined && prevProcessedRow[key] !== null && prevProcessedRow[key] !== "")) {
                    currentRowState[key] = prevProcessedRow[key];
                }
            });

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
    }, [data, numMaquinas, getAlumnoMachineInRow]);

    // --- ROW PAGINATION LOGIC ---
    // totalIterations now refers to the *original* number of iterations requested
    const totalIterationsFromOriginalData = data.length;
    // totalPages is calculated based on the original data length, to "lie" about the total pages
    const totalPages = Math.ceil(totalIterationsFromOriginalData / iterationsPerPage);

    // Get the iterations for the current page from the PROCESSED (and potentially limited) data
    const indexOfLastIteration = currentPage * iterationsPerPage;
    const indexOfFirstIteration = indexOfLastIteration - iterationsPerPage;
    
    // Only slice from processedData, which is already limited to MAX_ITERATIONS_DISPLAYED
    const currentDisplayedIterations = processedData.slice(indexOfFirstIteration, indexOfLastIteration);

    // --- DYNAMIC ALUMNO COLUMNS FOR THE CURRENT PAGE OF ITERATIONS ---
    const currentDisplayedAlumnoIds = useMemo(() => {
        let idsInCurrentPage = new Set();
        currentDisplayedIterations.forEach(row => {
            for (const key in row) {
                if (key.startsWith("Estado A") && row[key] !== null && row[key] !== undefined && row[key] !== "" && row[key] !== 'FS') {
                    idsInCurrentPage.add(key.replace("Estado ", ""));
                }
            }
        });
        return Array.from(idsInCurrentPage).sort((a, b) => {
            const numA = parseInt(a.replace('A', ''), 10);
            const numB = parseInt(b.replace('A', ''), 10);
            return numA - numB;
        });
    }, [currentDisplayedIterations]);

    // --- EFFECT TO RESET PAGE IF DATA CHANGES OR CURRENT PAGE BECOMES INVALID ---
    useEffect(() => {
        // If the current page is beyond the *actual* available processed data, or beyond the "faked" total pages,
        // reset it to the last valid page or 1.
        if (currentPage > totalPages && totalPages > 0) {
            setCurrentPage(totalPages);
        } else if (currentPage === 0 && totalPages > 0) {
            setCurrentPage(1);
        }
    }, [currentPage, totalPages]);

    // --- EARLY RETURN FOR NO DATA ---
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    // Get the last iteration data for the new table. This should still be from the *original* full data,
    // as the last iteration of the simulation is important regardless of display limits.
    const lastIteration = data.length > 0 ? data[data.length - 1] : null;

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
                            // Disable if current page is the last "faked" page
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
                                {currentDisplayedAlumnoIds.map(alumnoId => (
                                    <React.Fragment key={`alumno-subheader-${alumnoId}`}>
                                        <th>Estado</th>
                                        <th>Máquina</th>
                                    </React.Fragment>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {currentDisplayedIterations.map((fila, index) => {
                                const globalIndex = indexOfFirstIteration + index;
                                const originalDataRow = data[globalIndex]; // Access original row for non-propagated values

                                const isLlegadaEvent = fila.Evento.startsWith("Llegada Alumno");
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
                                    <tr key={`fila-${globalIndex}`}>
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

                                        <td>{renderIntValue(fila['Cola'])}</td>

                                        {[...Array(numMaquinas)].map((_, i) => (
                                            <td key={`estado-maquina-${i + 1}`}>
                                                {renderValue(fila[`Máquina ${i + 1}`])}
                                            </td>
                                        ))}

                                        {currentDisplayedAlumnoIds.map(alumnoId => {
                                            const estado = fila[`Estado ${alumnoId}`]; // Estado ya procesado
                                            // The machine assignment is part of the original data that caused the event
                                            const maquina = originalDataRow ? getAlumnoMachineInRow(alumnoId, originalDataRow) : null; 

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