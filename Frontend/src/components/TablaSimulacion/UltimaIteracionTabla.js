// UltimaIteracionTabla.js
import React from 'react';
import './tabla.css'; // Assuming you want to reuse the same CSS

function UltimaIteracionTabla({ lastIterationData, numMaquinas }) {
    if (!lastIterationData) {
        return <p>No hay datos para la última iteración.</p>;
    }

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

    const isLlegadaEvent = lastIterationData.Evento.startsWith("Llegada Alumno");

    // We need to pass the originalDataRow to UltimaIteracionTabla if it's not already merged into lastIterationData
    // For simplicity, let's assume lastIterationData already contains the necessary RND/Tiempo values for the last row
    // if not, you'd need to adjust how you pass data from TablaSimulacion.
    const rndLlegada = isLlegadaEvent ? lastIterationData['RND Llegada'] : null;
    const tiempoLlegada = isLlegadaEvent ? lastIterationData['Tiempo Llegada'] : null;

    const getInscripcionRND = (machineId) => {
        if ((lastIterationData?.Evento?.includes("Llegada Alumno") || lastIterationData?.Evento?.includes("Fin Inscripción")) && lastIterationData?.['Máquina'] === machineId) {
            return lastIterationData?.['RND Inscripción'];
        }
        return null;
    };

    const getInscripcionTiempo = (machineId) => {
        if ((lastIterationData?.Evento?.includes("Llegada Alumno") || lastIterationData?.Evento?.includes("Fin Inscripción")) && lastIterationData?.['Máquina'] === machineId) {
            return lastIterationData?.['Tiempo Inscripción'];
        }
        return null;
    };


    return (
        <div className="tabla-container ultima-iteracion-tabla">
            <h2>Última Iteración de la Simulación</h2>
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
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>{renderIntValue(lastIterationData['Iteracion'])}</td>
                            <td>{lastIterationData.Evento}</td>
                            <td>{renderValue(lastIterationData.Reloj)}</td>
                            <td>{renderValue(rndLlegada)}</td>
                            <td>{renderValue(tiempoLlegada)}</td>
                            <td>{renderValue(lastIterationData['Próxima Llegada'])}</td>

                            {[...Array(numMaquinas)].map((_, i) => {
                                const machineId = i + 1;
                                return (
                                    <React.Fragment key={`inscripcion-${machineId}`}>
                                        <td>{renderValue(getInscripcionRND(machineId))}</td>
                                        <td>{renderValue(getInscripcionTiempo(machineId))}</td>
                                        <td>
                                            {lastIterationData[`Fin Inscripción M${machineId}`] <= lastIterationData.Reloj
                                                ? '-'
                                                : renderValue(lastIterationData[`Fin Inscripción M${machineId}`])}
                                        </td>
                                    </React.Fragment>
                                );
                            })}

                            <td>{renderValue(lastIterationData?.['RND Tiempo Vuelta'])}</td>
                            <td>{renderValue(lastIterationData?.['Tiempo Vuelta'])}</td>
                            <td>
                                {lastIterationData['Próximo Inicio Mantenimiento'] <= lastIterationData.Reloj
                                    ? '-'
                                    : renderValue(lastIterationData['Próximo Inicio Mantenimiento'])}
                            </td>

                            <td>{renderValue(lastIterationData?.['RND Mantenimiento'])}</td>
                            <td>{renderValue(lastIterationData?.['Tiempo Mantenimiento'])}</td>
                            <td>
                                {lastIterationData['Fin Mantenimiento'] <= lastIterationData.Reloj
                                    ? '-'
                                    : renderValue(lastIterationData['Fin Mantenimiento'])}
                            </td>
                            <td>{renderIntValue(lastIterationData['Máquina Mant.'])}</td>

                            <td>{renderValue(lastIterationData['Acum. Tiempo Trabajado Tec.'])}</td>
                            <td>{renderValue(lastIterationData['Tiempo Ocioso Tec.'])}</td>
                            <td>
                                {renderValue(
                                    (parseFloat(lastIterationData['Tiempo Ocioso Tec.']) || 0) /
                                    ((parseFloat(lastIterationData['Acum. Tiempo Trabajado Tec.']) || 0) + (parseFloat(lastIterationData['Tiempo Ocioso Tec.']) || 0) || 1)
                                )}
                            </td>
                            <td>
                                {renderValue(
                                    ((parseFloat(lastIterationData['Tiempo Ocioso Tec.']) || 0) * 100) /
                                    ((parseFloat(lastIterationData['Acum. Tiempo Trabajado Tec.']) || 0) + (parseFloat(lastIterationData['Tiempo Ocioso Tec.']) || 0) || 1)
                                )}%
                            </td>

                            <td>{renderValue(lastIterationData['Cola'])}</td>

                            {[...Array(numMaquinas)].map((_, i) => (
                                <td key={`estado-maquina-${i + 1}`}>
                                    {renderValue(lastIterationData[`Máquina ${i + 1}`])}
                                </td>
                            ))}
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default UltimaIteracionTabla;