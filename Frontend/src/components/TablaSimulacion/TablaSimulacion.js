import React from 'react';
import './tabla.css';

function TablaSimulacion({ data }) {
    if (!Array.isArray(data) || data.length === 0) {
        return <p>Cargando datos o no hay datos disponibles...</p>;
    }

    const primeraIteracion = data[0];

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

                            {primeraIteracion.pcs.map((pc) => (
                                <th key={`pc-header-${pc.id}`} colSpan="3">PC {pc.id}</th>
                            ))}

                            <th colSpan="3">LLEGADA TÉCNICO</th>
                            <th colSpan="8">FIN MANTENIMIENTO</th>
                            <th rowSpan="2">ACUMULADOR ABANDONOS</th>
                            <th rowSpan="2">COLA DE ALUMNOS</th>

                            {primeraIteracion.pcs.map((pc) => (
                                <th key={`pc-estado-${pc.id}`} rowSpan="2">Estado PC {pc.id}</th>
                            ))}

                            {primeraIteracion.alumnos.map((alumno) => (
                                <th key={`alumno-${alumno.id}`} colSpan="2">Alumno {alumno.id}</th>
                            ))}
                        </tr>
                        <tr>
                            <th>RND</th>
                            <th>Tiempo Llegada</th>
                            <th>Próxima Llegada</th>

                            {primeraIteracion.pcs.map((pc) => (
                                <React.Fragment key={`pc-subheader-${pc.id}`}>
                                    <th>RND</th>
                                    <th>Tiempo Inscripción</th>
                                    <th>Fin Inscripción</th>
                                </React.Fragment>
                            ))}

                            <th>RND</th>
                            <th>Descanso</th>
                            <th>Regreso Técnico</th>

                            <th>RND</th>
                            <th>Estado</th>
                            <th>Tiempo Mantenimiento</th>
                            <th>Fin Mantenimiento</th>
                            <th>Última PC</th>
                            <th>Acum. Ocioso</th>
                            <th>Tiempo Total</th>
                            <th>Prom. Ocioso</th>

                            {primeraIteracion.alumnos.map((alumno) => (
                                <React.Fragment key={`alumno-subheader-${alumno.id}`}>
                                    <th>Estado</th>
                                    <th>PC Ocupada</th>
                                </React.Fragment>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((fila, index) => {
                            // Encontramos al alumno que generó el evento de llegada en esta fila
                            const alumnoLlegando = fila.alumnos?.find(
                                (alumno) => alumno.randomLlegada !== undefined
                            );

                            return (
                                <tr key={`fila-${index}`}>
                                    <td>{fila.iteracion}</td>
                                    <td>{fila.evento}</td>
                                    <td>{fila.reloj?.toFixed(2)}</td>

                                    <td>
                                        {alumnoLlegando?.randomLlegada !== undefined
                                            ? alumnoLlegando.randomLlegada.toFixed(2)
                                            : "-"}
                                    </td>
                                    <td>
                                        {alumnoLlegando?.duracionLlegada !== undefined
                                            ? alumnoLlegando.duracionLlegada.toFixed(2)
                                            : "-"}
                                    </td>
                                    <td>
                                        {fila.proximaLlegadaAlumno !== undefined
                                            ? fila.proximaLlegadaAlumno.toFixed(2)
                                            : "-"}
                                    </td>

                                    {fila.pcs.map((pc, i) => {
                                        const finInscripcion = fila[`finInscripcionPc${i + 1}`];
                                        return (
                                            <React.Fragment key={`pc-datos-${pc.id}`}>
                                                <td>{pc.numRandomInscripcion?.toFixed(2) || "-"}</td>
                                                <td>{pc.duracionInscripcion?.toFixed(2) || "-"}</td>
                                                <td>{finInscripcion?.toFixed(2) || "-"}</td>
                                            </React.Fragment>
                                        );
                                    })}

                                    <td>{fila.tecnico.numRandomRegreso?.toFixed(2) || "-"}</td>
                                    <td>{fila.tecnico.duracionDescanso?.toFixed(2) || "-"}</td>
                                    <td>{fila.proximaLlegadaTecnico?.toFixed(2) || "-"}</td>

                                    <td>{fila.tecnico.numRandomMantenimiento?.toFixed(2) || "-"}</td>
                                    <td>{fila.tecnico.estado || "-"}</td>
                                    <td>{fila.tecnico.duracionMantenimiento?.toFixed(2) || "-"}</td>
                                    <td>{fila.finMantenimiento?.toFixed(2) || "-"}</td>
                                    <td>{fila.tecnico.ultimaPcMantenida?.id || "-"}</td>
                                    <td>{fila.tecnico.acumTiempoOcioso?.toFixed(2) || "-"}</td>
                                    <td>{fila.tecnico.acumTiempoTotal?.toFixed(2) || "-"}</td>
                                    <td>{fila.tecnico.promedioTiempoOcioso?.toFixed(2) || "-"}</td>

                                    <td>{fila.acumAbandonos}</td>
                                    <td>{fila.colaAlumnos}</td>

                                    {fila.pcs.map((pc) => (
                                        <td key={`pc-estado-body-${pc.id}`}>{pc.estado}</td>
                                    ))}

                                    {fila.alumnos.map((alumno) => (
                                        <React.Fragment key={`alumno-datos-${alumno.id}`}>
                                            <td>{alumno.estado}</td>
                                            <td>{alumno.pcEnUso ? alumno.pcEnUso.id : "-"}</td>
                                        </React.Fragment>
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
