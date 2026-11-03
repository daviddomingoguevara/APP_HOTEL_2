# TRABAJO 2 · Resumen final

## Project structure
- TRABAJO 2/aplicacion-basica/ - JavaFX Version A (basic)
- TRABAJO 2/aplicacion-mejorada/ - JavaFX Version B (improved)
- TRABAJO 2/datos/ - hotel.xml, ejemplo-resultados.csv
- TRABAJO 2/memoria/ - memoria.md, metodologia.md, resultados.md, guion-presentacion.md
- TRABAJO 2/evidencias/capturas/ - screenshots clean
- TRABAJO 2/scripts/ - PowerShell scripts
- TRABAJO 2/servidor/ - ServidorMetricas.java
- TRABAJO 2/web/ - HTML/JS/CSS GitHub Pages
- presentacion.md (workspace root) - Gemini prompt clase

## Key accomplishments
1. Fixed fx:id missing in mejora.fxml → app B now launches
2. Both JavaFX apps compile + all 16 JUnit tests pass (8 per app)
3. Server running on port 8080, APIs, CORS, participant order A-B/B-A
4. Identical metrics recorded by both apps (nanoTime, searches, errors, success, abandonment, first-error, recovery, order, anon ID)
5. Web app responsive, GitHub Pages compatible, registration/results/CSV import/export/demo
6. Demo CSV validated: A 41.58s/85.7%/1.14 err; B 25.21s/100%/0.29 err; improvement 39.4%
7. README.md with PowerShell execution commands
8. Full memoria in Spanish with methodology, results template, presentation script
9. presentacion.md outside project as Gemini prompt (classroom focus, no backend)
10. Evidence cleaned: removed flawed screenshots, removed technical CSV, new clean captures
11. Room 103 = ocupada per XML, documented discrepancy
11. Formulas de métricas documentadas en web/js/metrics.js y memoria/

## Pending (noted in docs)
- No real participant experiment yet; data from automated tests + fictional demo CSV
- Memoria/resultados sections marked as template/pending
- Conclusions must not fabricate "B is better"; show negative improvements honestly
- Limitations documented: small sample, room 103 occupied, no subjective measure, no causal claim