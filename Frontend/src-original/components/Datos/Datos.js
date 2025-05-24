import Form from "react-bootstrap/Form";
import { Button, Row, Col, InputGroup, FormControl } from "react-bootstrap";
import * as formik from "formik";
import * as yup from "yup";

function Datos() {
    const { Formik } = formik;

    const schema = yup.object().shape({
        number: yup
            .number()
            .required("Se necesita ingresar un número")
            .positive("Número positivo")
            .integer("Debe ser un número entero")
            .max(1000000, "El número no puede ser mayor que 1000000"),
        option: yup.string().required("Please select an option"),
        terms: yup.bool().required().oneOf([true], "terms must be accepted"),
        rangeA: yup
            .number()
            .required("Valor A es requerido")
            .min(0, "El valor debe ser mayor o igual a 0.1")
            .max(1000000, "El valor debe ser menor a 1000000"),
        rangeB: yup
            .number()
            .required("Valor B es requerido")
            .min(0.1, "El valor debe ser mayor o igual a 0.1")
            .max(1000000, "El valor debe ser menor a 1000000")
            .when("rangeA", (rangeA, schema) => {
                return rangeA
                    ? schema.min(rangeA, "El valor B debe ser mayor que A")
                    : schema;
            }),
        lambda: yup
            .number()
            .required('Lambda es requerido')
            .min(0, "El valor debe ser mayor o igual a 0"),
        desv: yup
            .number()
            .required('La desviación estándar es requerida')
            .min(0, "El valor debe ser mayor o igual a 0"),
        media: yup
            .number()
            .required('La media es requerida')
            .min(0, "El valor debe ser mayor o igual a 0"),
        intervalos: yup
            .string()
            .required("Debe seleccionar un intervalo")
    });

    return (
        <Formik
            validationSchema={schema}
            onSubmit={(values) => {
                console.log("Formulario enviado con estos valores:", values); // Muestra los valores directamente
            }}
            initialValues={{
                number: "",
                option: "",
                rangeA: null,
                rangeB: null,
                desv: null,
                media: null,
                lambda: null,
                intervalos: null
            }}
        >
            {({ handleSubmit, handleChange, values, touched, errors, resetForm }) => {
                console.log("Valores del formulario:", values); // Agregar log para los valores en el renderizado
                return (
                    <Form
                        noValidate
                        onSubmit={(e) => {
                            e.preventDefault(); // Evitar el comportamiento por defecto del formulario
                            console.log("Botón de submit presionado");
                            handleSubmit(); // Llamar a handleSubmit para enviar el formulario
                        }}
                    >
                        <Row className="mb-3">
                            <Form.Group as={Col} md="4" controlId="validationFormikNumber">
                                <Form.Control
                                    type="number"
                                    name="number"
                                    value={values.number}
                                    onChange={handleChange}
                                    isInvalid={touched.number && !!errors.number}
                                    placeholder="Ingrese tamaño de muestra"
                                />
                                <Form.Control.Feedback type="invalid">
                                    {errors.number}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Row>

                        <Row className="mb-3">
                            <Form.Group as={Col} md="12" controlId="validationFormikIntervalos">
                                <div>
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="10 intervalos"
                                        name="intervalos"
                                        value="10"
                                        onChange={handleChange}
                                        checked={values.intervalos === "10"}
                                        isInvalid={touched.intervalos && !!errors.intervalos}
                                        id="intervalo10"
                                    />
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="15 intervalos"
                                        name="intervalos"
                                        value="15"
                                        onChange={handleChange}
                                        checked={values.intervalos === "15"}
                                        isInvalid={touched.intervalos && !!errors.intervalos}
                                        id="intervalo15"
                                    />
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="20 intervalos"
                                        name="intervalos"
                                        value="20"
                                        onChange={handleChange}
                                        checked={values.intervalos === "20"}
                                        isInvalid={touched.intervalos && !!errors.intervalos}
                                        id="intervalo20"
                                    />
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="30 intervalos"
                                        name="intervalos"
                                        value="30"
                                        onChange={handleChange}
                                        checked={values.intervalos === "30"}
                                        isInvalid={touched.intervalos && !!errors.intervalos}
                                        id="intervalo30"
                                    />
                                </div>
                                <Form.Control.Feedback type="invalid">
                                    {errors.intervalos}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Row>

                        <Row className="mb-3">
                            <Form.Group as={Col} md="12" controlId="validationFormikOptions">
                                <div>
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="Uniforme [a, b]"
                                        name="option"
                                        value="1"
                                        onChange={handleChange}
                                        checked={values.option === "1"}
                                        isInvalid={touched.option && !!errors.option}
                                        id="option1"
                                    />
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="Exponencial"
                                        name="option"
                                        value="2"
                                        onChange={handleChange}
                                        checked={values.option === "2"}
                                        isInvalid={touched.option && !!errors.option}
                                        id="option2"
                                    />
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="Normal"
                                        name="option"
                                        value="3"
                                        onChange={handleChange}
                                        checked={values.option === "3"}
                                        isInvalid={touched.option && !!errors.option}
                                        id="option3"
                                    />
                                </div>
                                <Form.Control.Feedback type="invalid">
                                    {errors.option}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Row>

                        {/* Dist Uniforme: [a,b] */}
                        {values.option === "1" && (
                            <Row className="mb-3">
                                <Col md="6">
                                    <InputGroup>
                                        <FormControl
                                            type="number"
                                            name="rangeA"
                                            placeholder="Valor a"
                                            value={values.rangeA}
                                            onChange={handleChange}
                                            isInvalid={touched.rangeA && !!errors.rangeA}
                                        />
                                        <FormControl
                                            type="number"
                                            name="rangeB"
                                            placeholder="Valor b"
                                            value={values.rangeB}
                                            onChange={handleChange}
                                            isInvalid={touched.rangeB && !!errors.rangeB}
                                        />
                                    </InputGroup>
                                    <FormControl.Feedback type="invalid">
                                        {errors.rangeA || errors.rangeB}
                                    </FormControl.Feedback>
                                </Col>
                            </Row>
                        )}

                        {/* Dist Exp: lambda */}
                        {values.option === "2" && (
                            <Row className="mb-3">
                                <Col md="6">

                                    <InputGroup>
                                        <FormControl
                                            type="number"
                                            name="lambda"
                                            placeholder="Lambda"
                                            value={values.lambda}
                                            onChange={handleChange}
                                            isInvalid={touched.lambda && !!errors.lambda}
                                        />
                                        <Form.Control.Feedback type="invalid">
                                            {errors.lambda}
                                        </Form.Control.Feedback>
                                    </InputGroup>
                                </Col>
                            </Row>
                        )}

                        {/* Dist Normal: Media, desviación estandar */}
                        {values.option === "3" && (
                            <Row className="mb-3">
                                <Col md="6">
                                    <Form.Label>Desviación estándar y Media</Form.Label>
                                    <InputGroup>
                                        <FormControl
                                            type="number"
                                            name="desv"
                                            placeholder="Desviación"
                                            value={values.desv}
                                            onChange={handleChange}
                                            isInvalid={touched.desv && !!errors.desv}
                                        />
                                        <FormControl.Feedback type="invalid">
                                            {errors.desv}
                                        </FormControl.Feedback>

                                        <FormControl
                                            type="number"
                                            name="media"
                                            placeholder="Media"
                                            value={values.media}
                                            onChange={handleChange}
                                            isInvalid={touched.media && !!errors.media}
                                        />
                                        <FormControl.Feedback type="invalid">
                                            {errors.media}
                                        </FormControl.Feedback>
                                    </InputGroup>
                                </Col>
                            </Row>
                        )}

                        <Button type="submit">Generar</Button>

                        <Button
                            variant="secondary"
                            className="ms-2"
                            onClick={() => resetForm()}
                        >
                            Limpiar
                        </Button>
                    </Form>
                );
            }}
        </Formik>
    );
}

export default Datos;
