import React, { useState, useMemo, useEffect } from 'react';
import './tabla.css';
import UltimaIteracionTabla from './UltimaIteracionTabla'; // Importa el nuevo componente

const variable = 1500; // No tocar

function TablaSimulacion({ data }) {
    const numMaquinas = 5; // Se asume un número fijo de máquinas

    // --- ESTADO DE PAGINACIÓN DE FILAS ---
    const [currentPage, setCurrentPage] = useState(1);
    const iterationsPerPage = 20; // Se quieren 20 líneas por página

    // --- FUNCIONES DE AYUDA PARA LA RENDERIZACIÓN ---
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

    // Función de ayuda para obtener la máquina a la que está asignado un alumno en una fila específica.
    const getAlumnoMachineInRow = (alumnoId, rowData) => {
        for (let j = 1; j <= numMaquinas; j++) {
            if (rowData[`Alumno M${j}`] === alumnoId) {
                return j;
            }
        }
        return null;
    };

    // --- Identifica la ÚLTIMA ITERACIÓN de toda la simulación ---
    // Esto se deriva de la prop `data` original, independientemente de la paginación o los límites.
    const lastIteration = useMemo(() => {
        return data && data.length > 0 ? data[data.length - 1] : null;
    }, [data]);

    // --- DATOS PROCESADOS (para la tabla principal, excluyendo la última iteración) ---
    const processedDataForMainTable = useMemo(() => {
        if (!Array.isArray(data) || data.length === 0) {
            return [];
        }

        // Excluye la última fila de los datos utilizados para la tabla principal
        const dataExcludingLast = data.slice(0, data.length - 1);
        const limitedData = dataExcludingLast.slice(0, variable); // Aquí se limita a 'variable' (1500)

        const keysToPropagate = [
            'Próxima Llegada',
            'Próximo Inicio Mantenimiento',
            'Acum. Tiempo Trabajado Tec.',
            'Tiempo Ocioso Tec.',
            'Cola',
            'Contador Alumnos', // Added for propagation
            'Contador Abandonos', // Added for propagation
            'Fin Mantenimiento',
            'Máquina Mant.',
        ];

        for (let i = 1; i <= numMaquinas; i++) {
            keysToPropagate.push(`Máquina ${i}`);
            keysToPropagate.push(`Fin Inscripción M${i}`);
        }

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

    // --- LÓGICA DE PAGINACIÓN DE FILAS ---
    // ¡Aquí está la "mentira"! totalIterations se basa en la longitud total de 'data'.
    const totalIterationsFromOriginalData = data.length; 
    const totalPages = Math.ceil((totalIterationsFromOriginalData-1) / iterationsPerPage);


    const indexOfLastIteration = currentPage * iterationsPerPage;
    const indexOfFirstIteration = indexOfLastIteration - iterationsPerPage;
    
    // Recorta de processedDataForMainTable, que ya excluye la última iteración original y está limitada por 'variable'
    const currentDisplayedIterations = processedDataForMainTable.slice(indexOfFirstIteration, indexOfLastIteration);

    // --- COLUMNAS DINÁMICAS DE ALUMNOS PARA LA PÁGINA ACTUAL DE ITERACIONES ---
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

    // --- EFECTO PARA REINICIAR LA PÁGINA SI LOS DATOS CAMBIAN O LA PÁGINA ACTUAL ES INVÁLIDA ---
    useEffect(() => {

        if (currentPage > totalPages && totalPages > 0) {
            setCurrentPage(totalPages);
        } else if (currentPage === 0 && totalPages > 0) {
            setCurrentPage(1);
        }
    }, [currentPage, totalPages]);

    // --- RETORNO TEMPRANO SI NO HAY DATOS ---
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    return (
        <>
            <div className="tabla-container">
                <h2>Estado de la Simulación</h2>
                {/* CONTROLES DE PAGINACIÓN DE FILAS */}
                {totalPages > 1 && (
                    <div className="pagination-controls">
                        <button
                            className='boton-copiar'
                            onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                            disabled={currentPage === 1}
                        >
                            Anterior Página
                        </button>
                        {/* Se muestra el número total de páginas basado en data.length */}
                        <span>Página {currentPage} de {totalPages}</span>
                        <button
                            className='boton-copiar'
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
                                <th rowSpan="2">CONTADOR ALUMNOS</th>
                                <th rowSpan="2">CONTADOR ABANDONOS</th>
                                <th rowSpan="2">% ABANDONOS</th>
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
                                // IMPORTANTE: globalIndex debe considerar la fila eliminada para lastIteration
                                // y la paginación para obtener la fila correcta de los datos originales.
                                const globalIndex = indexOfFirstIteration + index;
                                // Accede a originalDataRow del array `data` completo (no el `processedDataForMainTable`)
                                const originalDataRow = data[globalIndex]; 

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
                                        
                                        {/* NUEVAS CELDAS DE DATOS */}
                                        <td>{renderIntValue(fila['Contador Alumnos'])}</td>
                                        <td>{renderIntValue(fila['Contador Abandonos'])}</td>
                                        <td>
                                            {renderValue(
                                                (parseFloat(fila['Contador Alumnos']) > 0)
                                                    ? (parseFloat(fila['Contador Abandonos']) / parseFloat(fila['Contador Alumnos'])) * 100
                                                    : 0
                                            )}%
                                        </td>
                                        {/* FIN NUEVAS CELDAS DE DATOS */}

                                        {[...Array(numMaquinas)].map((_, i) => (
                                            <td key={`estado-maquina-${i + 1}`}>
                                                {renderValue(fila[`Máquina ${i + 1}`])}
                                            </td>
                                        ))}

                                        {currentDisplayedAlumnoIds.map(alumnoId => {
                                            const estado = fila[`Estado ${alumnoId}`]; // Estado ya procesado
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
            {/* Renderiza la nueva tabla para la última iteración */}
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