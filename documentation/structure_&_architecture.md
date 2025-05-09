# Arquitectura de la Aplicación y Estructura del Proyecto para "Mi Bolsillo App"

## 1. Arquitectura MVVM (Model-View-ViewModel)

Para "Mi Bolsillo App", adoptaremos la arquitectura **MVVM (Model-View-ViewModel)**. Esta elección se basa en sus múltiples beneficios, que incluyen una clara separación de responsabilidades, mejor testabilidad, mantenibilidad y una excelente integración con los componentes de Android Jetpack.

### Beneficios de MVVM:
* **Separación de Responsabilidades:** Divide la lógica de la aplicación en capas bien definidas (Modelo, Vista, Vista-Modelo).
* **Testabilidad:** Facilita la creación de pruebas unitarias para los ViewModels y la capa de Modelo.
* **Mantenibilidad:** Los cambios en una capa tienen un impacto reducido en las demás, simplificando la evolución de la app.
* **Compatibilidad con Jetpack:** Se integra de forma natural con `ViewModel`, `LiveData`/`StateFlow`, `Room`, `Navigation`, `Jetpack Compose` y `Hilt`.

### Componentes de MVVM en "Mi Bolsillo App":

#### a. View (Vista)
* **Implementación:** Se desarrollará utilizando **Jetpack Compose**. Las funciones Composable definirán la interfaz de usuario.
* **Responsabilidades:**
    * Mostrar los datos proporcionados por el ViewModel.
    * Reaccionar a los cambios de estado y actualizar la UI correspondientemente.
    * Capturar las interacciones del usuario (clics, gestos, entrada de texto) y comunicarlas al ViewModel.
    * Debe ser lo más "declarativa" y con la menor lógica posible.
* **Ejemplos:** `DashboardScreen()` (Composable), `AddTransactionScreen()` (Composable), `TransactionHistoryScreen()` (Composable).

#### b. ViewModel (Vista-Modelo)
* **Implementación:** Usaremos la clase `androidx.lifecycle.ViewModel` de Android Jetpack.
* **Responsabilidades:**
    * Actuar como intermediario entre la Vista (Composables) y el Modelo (Repositorios).
    * Contener la lógica de presentación y el estado de la UI.
    * Exponer el estado y los datos a la Vista mediante `StateFlow` (preferido con Compose) o `LiveData`.
    * Recibir eventos de la Vista y delegar las operaciones de datos al Modelo.
    * Sobrevivir a cambios de configuración.
    * No debe tener referencias directas a Composables específicos o al contexto de la UI (excepto el `Application` context si es estrictamente necesario para la inyección de dependencias o acceso a recursos).
* **Ejemplos:** `DashboardViewModel`, `AddTransactionViewModel`, `TransactionHistoryViewModel`.

#### c. Model (Modelo)
Es la capa encargada de los datos y la lógica de negocio de la aplicación.
* **Repositorios (Repositories):**
    * Actúan como la única fuente de verdad para un tipo de dato específico (ej. transacciones, categorías).
    * Abstraen el origen de los datos (base de datos local Room, futura API remota).
    * Proporcionan una API limpia para que los ViewModels accedan a los datos.
    * Ejemplos: `TransactionRepository`, `CategoryRepository`.
* **Fuentes de Datos (Data Sources):**
    * **Local:** Base de datos **Room** para la persistencia local.
        * **DAOs (Data Access Objects):** Interfaces que definen las operaciones de base de datos (CRUD) para cada entidad. Serán implementadas por Room.
    * **Remota:** (No contemplada para el MVP) Podría incluir servicios de red si la app se expandiera.
* **Entidades (Entities):**
    * Clases (generalmente `data class` de Kotlin) que representan las tablas en la base de datos Room (ej. `TransactionEntity`, `CategoryEntity`).
* **(Opcional) Casos de Uso / Interactors (Use Cases):**
    * Clases que encapsulan lógicas de negocio específicas y reutilizables si estas se vuelven demasiado complejas para un ViewModel o Repositorio.
    * Ejemplos: `GetMonthlySummaryUseCase`, `ValidateTransactionInputUseCase`.

### Flujo de Datos Típico en MVVM con Compose:
1.  El usuario interactúa con un Composable (**View**).
2.  El Composable notifica al **ViewModel** de la acción del usuario.
3.  El **ViewModel** procesa la acción. Si necesita datos o modificarlos, llama al **Repositorio**.
4.  El **Repositorio** accede a la **Fuente de Datos** (ej. DAO de Room).
5.  Los datos fluyen de vuelta al **ViewModel**.
6.  El **ViewModel** actualiza su `StateFlow` (o `LiveData`).
7.  El Composable (**View**), que recolecta (collects) el `StateFlow`, se recompone automáticamente para reflejar el nuevo estado/datos.

---

## 2. Estructura de Paquetes del Proyecto

Una estructura de paquetes organizada es vital para la mantenibilidad. Se propone la siguiente estructura base dentro de `com.mibolsilloapp`:

* **`data`**: Contiene toda la lógica de acceso y gestión de datos.
    * **`database`**: Clases de Room.
        * `dao`: Interfaces DAO (Ej: `TransactionDao.kt`).
        * `entity`: Clases Entity para Room (Ej: `TransactionEntity.kt`).
        * `AppDatabase.kt`: La clase principal de la base de datos Room.
    * **`model`**: (Opcional) Data classes que representan modelos de dominio si son diferentes de las Entities. Para esta app, las Entities de Room podrían ser suficientes inicialmente.
    * **`repository`**: Interfaces y sus implementaciones para los Repositorios (Ej: `TransactionRepository.kt`, `TransactionRepositoryImpl.kt`).

* **`ui`**: Contiene los elementos de la Interfaz de Usuario, principalmente funciones Composable y ViewModels.
    * Se recomienda subdividir por **característica (feature)**:
        * `ui.theme`: Auto-generado por Android Studio para Jetpack Compose, contiene la definición de `Color.kt`, `Theme.kt`, `Type.kt` (donde podrías definir tus estilos tipográficos basados en `visual_style.md`).
        * `ui.dashboard`: `DashboardScreen.kt` (Composable), `DashboardViewModel.kt`.
        * `ui.transaction_add_edit`: `AddEditTransactionScreen.kt`, `AddEditTransactionViewModel.kt`.
        * `ui.transaction_history`: `TransactionHistoryScreen.kt`, `TransactionHistoryViewModel.kt`, `TransactionItem.kt` (Composable para un ítem de la lista).
        * `ui.category_selection`: `CategorySelectionScreen.kt` (o Dialog Composable), `CategoryViewModel.kt`.
        * `ui.components`: Composables reutilizables genéricos (ej. botones personalizados, campos de texto estilizados).
        * `MainActivity.kt`: La actividad principal que alojará los Composable Screens.

* **`domain`** (o **`usecases`**): (Opcional) Para los Casos de Uso, si se decide implementarlos para lógicas de negocio complejas.

* **`di`**: Para la Inyección de Dependencias.
    * Contendrá los módulos de **Hilt** para proveer las dependencias necesarias a través de la aplicación (ej. proveer instancias de Repositorios a los ViewModels, o DAOs a los Repositorios).

* **`utils`**: Clases de utilidad, funciones de extensión, constantes, formateadores, etc.

---

## 3. Tecnologías Clave Adicionales

* **Jetpack Compose:** Se utilizará para construir la interfaz de usuario de forma declarativa y moderna en Kotlin. Los elementos de la capa `View` serán principalmente funciones Composable.
* **Hilt:** Se utilizará para la Inyección de Dependencias, facilitando la gestión de dependencias y mejorando la testabilidad y modularidad del código, especialmente en la interacción entre ViewModels, Repositorios y Fuentes de Datos.

---