import { useState } from 'react';
import { Formik } from 'formik';
import * as Yup from 'yup';
import { Button, Form, Row, Col, InputGroup, FormControl } from 'react-bootstrap';
import axios from 'axios';
import './ObtenerSimulacion.css'

function ObtenerSimulacion({ setDatosGenerados }) {
    const [isLoading, setIsLoading] = useState(false);

    const schema = Yup.object().shape({
        // Validaciones para "Cantidad de minutos a simular"
        minutosSimulacion: Yup
            .number()
            .required("Se necesita ingresar los minutos")
            .positive("Número positivo")
            .moreThan(0, "El número debe ser mayor a 0"),

        // Validaciones para "Mostrar desde minuto"
        minutoDesde: Yup
            .number()
            .required("Se necesita ingresar el minuto inicial")
            .min(0, "Debe ser mayor o igual a 0"),

        // Validaciones para "Cantidad de iteraciones"
        iteraciones: Yup
            .number()
            .required("Se necesita ingresar las iteraciones")
            .integer("Debe ser un número entero")
            .min(1, "Debe ser al menos 1"),

        //validaciones para Llegada de alumnos
        mediaLlegada: Yup.number()
            .required("Se necesita ingresar la media")
            .positive("Número positivo")
            .moreThan(0, "El número debe ser mayor a 0"),

        //validaciones para Tiempo de inscripción
        tiempoMinimoInscripcion: Yup.number()
            .required("Se necesita ingresar el tiempo de inscripción")
            .test(
                'tiempoMinimo-lessThanOrEqualToB',
                'Tiempo mínimo no puede ser mayor que Tiempo máximo',
                function (value) {
                    const { tiempoMaximoInscripcion } = this.parent;
                    if (value && tiempoMaximoInscripcion && value > tiempoMaximoInscripcion) {
                        return false; // Si A es mayor que B, retorna false
                    }
                    return true; // Si no, la validación pasa
                }
            ),

        tiempoMaximoInscripcion: Yup.number()
            .required("Se necesita ingresar el tiempo de inscripción")
            .test(
                'tiempoMaximo-not-equal-to-A',
                'Tiempo máximo no puede ser igual a Tiempo mínimo',
                function (value) {
                    const { tiempoMinimoInscripcion } = this.parent;
                    if (value && tiempoMinimoInscripcion && value === tiempoMinimoInscripcion) {
                        return false; // Retorna false si A es igual a B
                    }
                    return true;
                }
            ),

        // Validaciones para Mantenimiento por PC
        tiempoMinimoMantenimiento: Yup.number()
            .required("Se necesita ingresar el tiempo de mantenimiento")
            .test(
                'tiempoMinimo-lessThanOrEqualToB',
                'Tiempo mínimo no puede ser mayor que Tiempo máximo',
                function (value) {
                    const { tiempoMaximoMantenimiento } = this.parent;
                    if (value && tiempoMaximoMantenimiento && value > tiempoMaximoMantenimiento) {
                        return false; // Si A es mayor que B, retorna false
                    }
                    return true; // Si no, la validación pasa
                }
            ),

        tiempoMaximoMantenimiento: Yup.number()
            .required("Se necesita ingresar el tiempo de mantenimiento")
            .test(
                'tiempoMaximo-not-equal-to-A',
                'Tiempo máximo no puede ser igual a Tiempo mínimo',
                function (value) {
                    const { tiempoMinimoMantenimiento } = this.parent;
                    if (value && tiempoMinimoMantenimiento && value === tiempoMinimoMantenimiento) {
                        return false; // Retorna false si A es igual a B
                    }
                    return true;
                }
            ),

        // Validaciones para el técnico
        minutosBaseTecnico: Yup
            .number()
            .required("Se necesita ingresar los minutos base")
            .positive("Número positivo")
            .moreThan(0, "El número debe ser mayor a 0"),

        minutosRangoTecnico: Yup
            .number()
            .required("Se necesita ingresar el rango de minutos")
            .min(0, "Debe ser mayor o igual a 0"),
    });


    const fetchDataFromBackend = async (values) => {
        setIsLoading(true);
    
        // Extraemos los valores del formulario
        const {
            tiempoMinimoInscripcion,
            tiempoMaximoInscripcion,
            mediaLlegada,
            tiempoMinimoMantenimiento,
            tiempoMaximoMantenimiento,
            minutosBaseTecnico,
            minutosRangoTecnico,
            minutosSimulacion,
            minutoDesde,
            iteraciones
        } = values;
    
        // Armamos la URL con los parámetros como query params
        const url = `http://localhost:8080/simulacion/run-parametros?` +
            `minInscripcion=${tiempoMinimoInscripcion}&` +
            `maxInscripcion=${tiempoMaximoInscripcion}&` +
            `mediaLlegada=${mediaLlegada}&` +
            `minMantenimiento=${tiempoMinimoMantenimiento}&` +
            `maxMantenimiento=${tiempoMaximoMantenimiento}&` +
            `baseRegresoTecnico=${minutosBaseTecnico}&` +
            `rangoRegresoTecnico=${minutosRangoTecnico}&` +
            `minutosSimulacion=${minutosSimulacion}&` +
            `minutoDesde=${minutoDesde}&` +
            `iteracionesMostrar=${iteraciones}`;
    
        try {
            const response = await axios.get(url);
            setDatosGenerados({
                data: response.data
            });
        } catch (error) {
            console.error('Error al obtener los datos:', error);
        } finally {
            setIsLoading(false);
        }
    };
    

    return (
        <Formik
            initialValues={{
                mediaLlegada: 2,
                tiempoMinimoInscripcion: 5,
                tiempoMaximoInscripcion: 8,
                tiempoMinimoMantenimiento: 3,
                tiempoMaximoMantenimiento: 10,
                minutosBaseTecnico: 60,
                minutosRangoTecnico: 3,
                minutosSimulacion: '',
                minutoDesde: '',
                iteraciones: ''
            }}
            validationSchema={schema}
            onSubmit={(values, { setSubmitting }) => {
                console.log("Formulario enviado con los valores:", values);
                fetchDataFromBackend(values);  // Función para hacer la petición
                setSubmitting(false);
            }}
        >
            {({ handleSubmit, handleChange, values, touched, errors, resetForm }) => (
                <Form noValidate onSubmit={handleSubmit} className='form'>
                    <h1>Inscripción a exámenes</h1>
                    {/* Llegada de alumnos */}
                    <Row >
                        <Col md="6">
                            <Form.Label>Llegada de alumnos</Form.Label>

                            <InputGroup className="opcionales">
                                <FormControl
                                    className='caja-inputs'
                                    type="number"
                                    name="mediaLlegada"
                                    placeholder="Media"
                                    value={values.mediaLlegada || ''}
                                    onChange={handleChange}
                                    isInvalid={touched.mediaLlegada && !!errors.mediaLlegada}
                                />
                                <Form.Control.Feedback type="invalid" className='errores'>
                                    {errors.mediaLlegada}
                                </Form.Control.Feedback>
                            </InputGroup>
                        </Col>
                    </Row>

                    {/* Tiempo de inscripción */}
                    <Row >
                        <Col md="6">
                            <Form.Label>Tiempo de inscripción</Form.Label>
                            <InputGroup className="opcionales">
                                <FormControl
                                    className='caja-inputs'
                                    type="number"
                                    name="tiempoMinimoInscripcion"
                                    placeholder="Tiempo mínimo"
                                    value={values.tiempoMinimoInscripcion !== undefined ? values.tiempoMinimoInscripcion : ''}
                                    onChange={handleChange}
                                    isInvalid={touched.tiempoMinimoInscripcion && !!errors.tiempoMinimoInscripcion}
                                />
                                <FormControl
                                    className='caja-inputs'
                                    type="number"
                                    name="tiempoMaximoInscripcion"
                                    placeholder="Tiempo máximo"
                                    value={values.tiempoMaximoInscripcion !== undefined ? values.tiempoMaximoInscripcion : ''}
                                    onChange={handleChange}
                                    isInvalid={touched.tiempoMaximoInscripcion && !!errors.tiempoMaximoInscripcion}
                                />
                            </InputGroup>
                            <FormControl.Feedback type="invalid" className='errores'>
                                {errors.tiempoMinimoInscripcion || errors.tiempoMaximoInscripcion}
                            </FormControl.Feedback>
                        </Col>
                    </Row>

                    <h1>Mantenimiento</h1>
                    {/* Mantenimiento por PC */}
                    <Row >
                        <Col md="6">
                            <Form.Label>Mantenimiento por PC</Form.Label>
                            <InputGroup className="opcionales">
                                <FormControl
                                    className='caja-inputs'
                                    type="number"
                                    name="tiempoMinimoMantenimiento"
                                    placeholder="Tiempo mínimo"
                                    value={values.tiempoMinimoMantenimiento !== undefined ? values.tiempoMinimoMantenimiento : ''}
                                    onChange={handleChange}
                                    isInvalid={touched.tiempoMinimoMantenimiento && !!errors.tiempoMinimoMantenimiento}
                                />
                                <FormControl
                                    className='caja-inputs'
                                    type="number"
                                    name="tiempoMaximoMantenimiento"
                                    placeholder="Tiempo máximo"
                                    value={values.tiempoMaximoMantenimiento !== undefined ? values.tiempoMaximoMantenimiento : ''}
                                    onChange={handleChange}
                                    isInvalid={touched.tiempoMaximoMantenimiento && !!errors.tiempoMaximoMantenimiento}
                                />
                            </InputGroup>
                            <FormControl.Feedback type="invalid" className='errores'>
                                {errors.tiempoMinimoMantenimiento || errors.tiempoMaximoMantenimiento}
                            </FormControl.Feedback>
                        </Col>
                    </Row>

                    {/* Regreso del técnico */}
                    <Row >
                        <Col md="6">
                            <Form.Label>Regreso del técnico</Form.Label>
                            <InputGroup className="opcionales">
                                <FormControl
                                    className='caja-inputs'
                                    type="number"
                                    name="minutosBaseTecnico"
                                    placeholder="Tiempo Base"
                                    value={values.minutosBaseTecnico !== undefined ? values.minutosBaseTecnico : ''}
                                    onChange={handleChange}
                                    isInvalid={touched.minutosBaseTecnico && !!errors.minutosBaseTecnico}
                                />
                                <FormControl
                                    className='caja-inputs'
                                    type="number"
                                    name="minutosRangoTecnico"
                                    placeholder="Rango de Tiempo"
                                    value={values.minutosRangoTecnico !== undefined ? values.minutosRangoTecnico : ''}
                                    onChange={handleChange}
                                    isInvalid={touched.minutosRangoTecnico && !!errors.minutosRangoTecnico}
                                />
                            </InputGroup>
                            <FormControl.Feedback type="invalid" className='errores'>
                                {errors.minutosBaseTecnico || errors.minutosRangoTecnico}
                            </FormControl.Feedback>
                        </Col>
                    </Row>


                    <h1>Simulación</h1>
                    {/* X: Cantidad de minutos a simular */}
                    <Row className='seccion-simulacion'>
    {/* Cantidad de minutos a simular */}
    <Col md={4}>
        <Form.Label className='subtitulo'>Cantidad de minutos a simular</Form.Label>
        <Form.Control
            className='caja-inputs'
            type="number"
            name="minutosSimulacion"
            placeholder="Ingrese los minutos"
            value={values.minutosSimulacion}
            onChange={handleChange}
            isInvalid={touched.minutosSimulacion && !!errors.minutosSimulacion}
        />
        <Form.Control.Feedback type="invalid" className='errores'>
            {errors.minutosSimulacion}
        </Form.Control.Feedback>
    </Col>

    {/* Mostrar desde minuto */}
    <Col md={4}>
        <Form.Label className='subtitulo'>Mostrar desde minuto</Form.Label>
        <Form.Control
            className='caja-inputs'
            type="number"
            name="minutoDesde"
            placeholder="Ingrese los minutos"
            value={values.minutoDesde}
            onChange={handleChange}
            isInvalid={touched.minutoDesde && !!errors.minutoDesde}
        />
        <Form.Control.Feedback type="invalid" className='errores'>
            {errors.minutoDesde}
        </Form.Control.Feedback>
    </Col>

    {/* Cantidad de iteraciones */}
    <Col md={4}>
        <Form.Label className='subtitulo'>Cantidad de iteraciones</Form.Label>
        <Form.Control
            className='caja-inputs'
            type="number"
            name="iteraciones"
            placeholder="Ingrese las iteraciones"
            value={values.iteraciones}
            onChange={handleChange}
            isInvalid={touched.iteraciones && !!errors.iteraciones}
        />
        <Form.Control.Feedback type="invalid" className='errores'>
            {errors.iteraciones}
        </Form.Control.Feedback>
    </Col>
</Row>

                    {/* Botones */}
                    <div className='boton-grp'>
                        <Button
                            variant="secondary"
                            className="boton-limpiar"
                            onClick={() => resetForm()}
                        >
                            Limpiar
                        </Button>

                        <Button type="submit" disabled={isLoading} className='boton'>
                            {isLoading ? 'Generando...' : 'Simular'}
                        </Button>
                    </div>
                </Form>
            )}
        </Formik>
    );
}

export default ObtenerSimulacion;