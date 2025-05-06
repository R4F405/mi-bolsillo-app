# Flujos de Usuario de Mi Bolsillo App

## Flujo: Registrar Nueva Transacción (Ingreso o Gasto)

1.  **Punto de Partida:** Usuario en Pantalla Principal/Dashboard o Lista de Transacciones, pulsa botón "+".
2.  **Transición:** Sistema navega a Pantalla "Nueva Transacción".
3.  **En Pantalla "Nueva Transacción":**
    * Usuario **selecciona el Tipo** ("Ingreso" o "Gasto" - con "Gasto" posiblemente preseleccionado mediante un Segmented Control).
    * Usuario introduce Monto.
    * Usuario selecciona/confirma Fecha (usando selector de fecha).
    * Usuario pulsa un campo/botón "Categoría" que lleva a la Pantalla "Selección de Categoría". Selecciona una categoría y vuelve.
    * (Opcional) Usuario introduce Descripción.
    * Botón "Confirmar Transacción" (o texto dinámico "Confirmar Gasto" / "Confirmar Ingreso").
4.  **Acción del Usuario:** Pulsa "Confirmar Transacción".
5.  **Procesamiento:** Validación de datos. Si hay errores, muestra mensaje en la misma pantalla. Si es correcto, guarda la transacción.
6.  **Feedback y Siguiente Paso:** Muestra mensaje de confirmación breve (Toast/Snackbar). Permanece en la pantalla "Nueva Transacción" con campos de entrada limpios (Monto, Descripción). El Tipo y la Categoría podrían mantenerse preseleccionados de la última entrada para agilizar registros consecutivos.

---

