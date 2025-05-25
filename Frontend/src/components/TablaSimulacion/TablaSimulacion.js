import React from 'react';
import './tabla.css';

function TablaSimulacion({ data }) {
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    const primeraIteracion = data[0];
    const numMaquinas = 6;
    const maxAlumnos = primeraIteracion.max_alumnos || 0;

    const renderValue = (value) => {
        if (typeof value === 'number') {
            return value.toFixed(2);
        }
        return value || "-";
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

                            <th colSpan="3">INICIO MANTENIMIENTO</th>
                            <th colSpan="6">FIN MANTENIMIENTO / PRÓX. CICLO</th>

                            <th rowSpan="2">COLA DE ALUMNOS</th>
                            <th rowSpan="2">Tiempo Espera Acumulado</th>
                            <th rowSpan="2">Tiempo Espera Promedio</th>

                            {[...Array(numMaquinas)].map((_, i) => (
                                <th key={`maquina-estado-header-${i + 1}`} rowSpan="2">Estado Máquina {i + 1}</th>
                            ))}

                            {[...Array(maxAlumnos)].map((_, i) => (
                                <th key={`alumno-header-${i + 1}`} colSpan="2">Alumno A{i + 1}</th>
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

                            <th>RND Mant.</th>
                            <th>Tiempo Mant.</th>
                            <th>Próx. Inicio Mant.</th>

                            <th>RND Vuelta</th>
                            <th>Tiempo Vuelta</th>
                            <th>RND Mant. Sched.</th>
                            <th>Tiempo Mant. Sched.</th>
                            <th>Fin Mant.</th>
                            <th>Máquina Mant.</th>

                            {[...Array(maxAlumnos)].map((_, i) => (
                                <React.Fragment key={`alumno-subheader-${i + 1}`}>
                                    <th>Estado</th>
                                    <th>Tiempo Espera</th>
                                </React.Fragment>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((fila, index) => {
                            // Add a check here:
                            const eventoString = fila.Evento || ""; // Ensure fila.Evento is a string, default to empty
                            const eventoMaquinaId = fila.Máquina !== "N/A" ? parseInt(fila.Máquina) : null;
                            const isLlegadaEvent = eventoString.startsWith("Llegada Alumno");
                            const isFinInscripcionEvent = eventoString.startsWith("Fin Inscripción");

                            let rndMantenimientoDisplay = "N/A";
                            let tiempoMantenimientoDisplay = "N/A";
                            let finMantenimientoDisplay = "N/A";
                            let maquinaMantenimientoDisplay = "N/A";

                            if (eventoString === "Inicializacion" || eventoString.startsWith("Inicio Mantenimiento")) {
                                rndMantenimientoDisplay = fila['RND Mantenimiento'];
                                tiempoMantenimientoDisplay = fila['Tiempo Mantenimiento'];
                                finMantenimientoDisplay = fila['Fin Mantenimiento'];
                                maquinaMantenimientoDisplay = fila.Máquina;
                            } else if (eventoString.startsWith("Fin Mantenimiento")) {
                                maquinaMantenimientoDisplay = fila.Máquina;
                                finMantenimientoDisplay = fila['Fin Mantenimiento'];
                                if (fila['RND Mantenimiento'] && fila['Tiempo Mantenimiento']) {
                                    rndMantenimientoDisplay = fila['RND Mantenimiento'];
                                    tiempoMantenimientoDisplay = fila['Tiempo Mantenimiento'];
                                }
                            }

                            return (
                                <tr key={`fila-${index}`}>
                                    <td>{index + 1}</td>
                                    <td>{fila.Evento}</td>
                                    <td>{renderValue(fila.Reloj)}</td>

                                    <td>{isLlegadaEvent ? renderValue(fila['RND Llegada']) : "-"}</td>
                                    <td>{isLlegadaEvent ? renderValue(fila['Tiempo Llegada']) : "-"}</td>
                                    <td>{renderValue(fila['Próxima Llegada'])}</td>

                                    {[...Array(numMaquinas)].map((_, i) => {
                                        const currentMachineId = i + 1;
                                        const displayInscripcionData = (isLlegadaEvent || isFinInscripcionEvent) && eventoMaquinaId === currentMachineId;

                                        return (
                                            <React.Fragment key={`maquina-inscripcion-data-${currentMachineId}`}>
                                                <td>{displayInscripcionData ? renderValue(fila['RND Inscripción']) : "-"}</td>
                                                <td>{displayInscripcionData ? renderValue(fila['Tiempo Inscripción']) : "-"}</td>
                                                <td>{displayInscripcionData ? renderValue(fila['Fin Inscripción']) : "-"}</td>
                                            </React.Fragment>
                                        );
                                    })}

                                    <td>{renderValue(rndMantenimientoDisplay)}</td>
                                    <td>{renderValue(tiempoMantenimientoDisplay)}</td>
                                    <td>{renderValue(fila['Próximo Inicio Mantenimiento'])}</td>

                                    <td>{renderValue(fila['RND Tiempo Vuelta'])}</td>
                                    <td>{renderValue(fila['Tiempo Vuelta'])}</td>
                                    <td>{renderValue(fila['RND Mantenimiento'])}</td>
                                    <td>{renderValue(fila['Tiempo Mantenimiento'])}</td>
                                    <td>{renderValue(fila['Fin Mantenimiento'])}</td>
                                    <td>{renderValue(maquinaMantenimientoDisplay)}</td>


                                    <td>{renderValue(fila.Cola)}</td>
                                    <td>{renderValue(fila['Tiempo Espera Acumulado'])}</td>
                                    <td>{renderValue(fila['Tiempo Espera Promedio'])}</td>

                                    {[...Array(numMaquinas)].map((_, i) => (
                                        <td key={`maquina-estado-body-${i + 1}`}>
                                            {fila[`Máquina ${i + 1}`] || "-"}
                                        </td>
                                    ))}

                                    {[...Array(maxAlumnos)].map((_, i) => {
                                        const alumnoId = `A${i + 1}`;
                                        return (
                                            <React.Fragment key={`alumno-data-${alumnoId}`}>
                                                <td>{fila[`Estado ${alumnoId}`] || "-"}</td>
                                                <td>{renderValue(fila[`Tiempo Espera ${alumnoId}`])}</td>
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