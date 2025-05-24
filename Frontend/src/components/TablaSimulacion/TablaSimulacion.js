import React, { useState, useEffect } from 'react';
import './tabla.css';

function TablaSimulacion({ data, numBins }) {
    const [frequencies, setFrequencies] = useState([]);
    const [totalFrequency, setTotalFrequency] = useState(0);

    useEffect(() => {
        if (!data || data.length === 0) return;

        const min = data.reduce((a, b) => Math.min(a, b), Infinity);
        const max = data.reduce((a, b) => Math.max(a, b), -Infinity);
        const binWidth = (max - min) / numBins;

        const bins = Array(numBins).fill(0);
        const intervals = [];
        let total = 0;

        data.forEach(value => {
            let binIndex = Math.floor((value - min) / binWidth);
            if (binIndex === numBins) binIndex--; 
            bins[binIndex]++;
        });

        for (let i = 0; i < numBins; i++) {
            const start = min + i * binWidth;
            const end = start + binWidth;
            intervals.push({
                interval: `${start.toFixed(2)} - ${end.toFixed(2)}`,
                frequency: bins[i],
            });
            total += bins[i];
        }

        setFrequencies(intervals);
        setTotalFrequency(total);
    }, [data, numBins]);

    return (
        <div className='tabla-container'>
            <h2>Tabla de Frecuencias</h2>
            <table border="1" className='tabla-frecuencias'>
                <thead>
                    <tr>
                        <th>Intervalo</th>
                        <th>Frecuencia</th>
                    </tr>
                </thead>
                <tbody>
                    {frequencies.map((row, index) => (
                        <tr key={index}>
                            <td>{row.interval}</td>
                            <td>{row.frequency}</td>
                        </tr>
                    ))}
                    <tr>
                        <td><strong>Total</strong></td>
                        <td><strong>{totalFrequency}</strong></td>
                    </tr>
                </tbody>
            </table>
        </div>
    );
}

export default TablaSimulacion;
