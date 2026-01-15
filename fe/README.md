# Laboratory Management System — Frontend

This is the frontend for the Laboratory Management System built with **React**, **TypeScript**, and **Vite**. It provides a modular, scalable architecture for managing authentication, patient records, test orders, instruments, and warehouse operations.

---

## Tech Stack

- **React** + **TypeScript**
- **Vite** for fast bundling
- **React Router** for client-side routing
- **[Material UI](https://mui.com/material-ui/)** + **[MUI Icons](https://mui.com/material-ui/material-icons/)** for components

---

## Project Setup

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

---

## Routing

Routes are centralized in `src/routes/index.tsx` using `<Routes>` and `<Route>` from `react-router-dom`. Each domain-specific service (e.g. Test Orders, Patients, Instruments) registers its own routes in a dedicated `[Service]Route.tsx` file.

### Common fault-tolerant pages:
- `/unauthorized` → Unauthorized access
- `/server-error` → Server error fallback
- `*` → Catch-all for 404 Not Found

---

## Notes

- **`src/features/`**: Contains domain-specific modules like `User`, `Patient`, `TestOrder`, etc. Each feature is self-contained with its own `components/` and `pages/`.
- **`src/pages/`**: Holds **global pages** that are not tied to any specific feature. These include:
  - `Home.tsx`: The landing page.
  - `NotFound.tsx`: A 404 fallback for unknown routes.
  - `ServerError.tsx`: A generic error page for server-side failures.
  - `Unauthorized.tsx`: Shown when access is denied due to lack of permissions.
- **`src/routes/`**: Centralizes all route definitions. Each service has its own `[Service]Route.tsx` file (e.g. `TestOrderRoute.tsx`, `PatientRoute.tsx`) to register all pages under `/[service]`.
- **`src/api/`**: Centralized API configuration and token management.
  - `AxiosInstance.ts`: Initializes a shared Axios client with interceptors for automatic token injection and refresh handling.
  - `AuthRefresh.ts`: Handles refresh token logic.
  - `Endpoints.ts`: Defines base URLs and service-specific endpoints for all backend services (e.g., Patient, Test Order, etc.).
- **`src/layouts/`**: Reusable layout wrappers for nesting routes with consistent UI:
  - `GeneralLayout.tsx`: Used for public-facing or general pages.
  - `DashboardLayout.tsx`: Used for internal tools like dashboards, logs, and history views.
  - `AuthLayout.tsx`: Used for authentication flows.
- **`src/components/`**: Shared UI components used across multiple features, such as `Header`, `Navbar`, `SearchBar`, `DashboardSidebar`, and `ProtectedRoute`.
- **`src/types/`**: Shared types/interfaces, such as `BaseResponse`.
- **`src/assets/`**: Static assets like logos or icons.
- **`main.tsx`**: Entry point for rendering the React app.
- **`App.tsx`**: Root component that loads the centralized routing tree.

---

## Project Structure

```plaintext
fe
├─ eslint.config.js
├─ index.html
├─ package-lock.json
├─ package.json
├─ public
│  ├─ HemaLabManager - Altered.png
│  ├─ HemaLabManager - Full.png
│  ├─ HemaLabManager - Logo.png
│  └─ vite.svg
├─ README.md
├─ src
│  ├─ api
│  │  ├─ AuthRefresh.ts
│  │  ├─ AxiosInstance.ts
│  │  └─ Endpoints.ts
│  ├─ App.css
│  ├─ App.tsx
│  ├─ assets
│  │  ├─ medical-laboratory (1).svg
│  │  └─ react.svg
│  ├─ components
│  │  ├─ Breadcrumbs.tsx
│  │  ├─ DashboardSidebar.tsx
│  │  ├─ DynamicAddUpdateForm.tsx
│  │  ├─ DynamicFilterPanel.tsx
│  │  ├─ Header.tsx
│  │  ├─ Navbar.tsx
│  │  ├─ ProtectedRoute.tsx
│  │  ├─ SearchBar.tsx
│  │  └─ ui
│  │     ├─ alert
│  │     │  └─ Alert.tsx
│  │     └─ toast
│  │        └─ ToastContext.tsx
│  ├─ context
│  │  ├─ AuthContext.tsx
│  │  ├─ ThemeButtonToggle.tsx
│  │  └─ ThemeContext.tsx
│  ├─ features
│  │  ├─ Instrument
│  │  │  ├─ components
│  │  │  │  ├─ Analysis
│  │  │  │  │  ├─ AnalysisControlPanel.tsx
│  │  │  │  │  ├─ AnalysisSkeleton.tsx
│  │  │  │  │  ├─ DeleteRawDataModal.tsx
│  │  │  │  │  ├─ EstimateModal.tsx
│  │  │  │  │  ├─ RawHl7DataViewerModal.tsx
│  │  │  │  │  ├─ RawResultsTable.tsx
│  │  │  │  │  └─ SampleAnalysisForm.tsx
│  │  │  │  ├─ Configuration
│  │  │  │  │  └─ ConfigurationDisplay.tsx
│  │  │  │  ├─ Mode
│  │  │  │  │  ├─ InstrumentModeDisplay.tsx
│  │  │  │  │  └─ ModeSwitchModal.tsx
│  │  │  │  ├─ Reagent
│  │  │  │  │  └─ ReagentManagement.tsx
│  │  │  │  └─ Sync
│  │  │  │     └─ ConfigSyncButton.tsx
│  │  │  ├─ pages
│  │  │  │  ├─ InstrumentDashboard.tsx
│  │  │  │  └─ InstrumentHome.tsx
│  │  │  ├─ services
│  │  │  │  └─ InstrumentAPI.ts
│  │  │  └─ types
│  │  │     └─ types.ts
│  │  ├─ Monitoring
│  │  │  ├─ components
│  │  │  │  ├─ EventLogDetailModal.tsx
│  │  │  │  ├─ EventLogFilterPanel.tsx
│  │  │  │  ├─ EventLogsTable.tsx
│  │  │  │  ├─ HealthEventLogsDetailModal.tsx
│  │  │  │  ├─ HealthEventLogsFilterPanel.tsx
│  │  │  │  ├─ HealthEventLogsTable.tsx
│  │  │  │  ├─ HL7BackupDetailModal.tsx
│  │  │  │  ├─ HL7BackupFilterPanel.tsx
│  │  │  │  ├─ HL7BackupTable.tsx
│  │  │  │  ├─ MessageBrokerHealthDetailModal.tsx
│  │  │  │  ├─ MessageBrokerHealthFilterPanel.tsx
│  │  │  │  ├─ MessageBrokerHealthTable.tsx
│  │  │  │  ├─ SyncUpRequestsDetailModal.tsx
│  │  │  │  ├─ SyncUpRequestsFilterPanel.tsx
│  │  │  │  └─ SyncUpRequestsTable.tsx
│  │  │  ├─ pages
│  │  │  │  ├─ EventLogsListPage.tsx
│  │  │  │  ├─ HealthEventLogsListPage.tsx
│  │  │  │  ├─ HL7BackupListPage.tsx
│  │  │  │  ├─ MessageBrokerHealthListPage.tsx
│  │  │  │  └─ SyncUpRequestsListPage.tsx
│  │  │  ├─ service
│  │  │  │  ├─ eventLogsApiService.ts
│  │  │  │  ├─ healthEventLogsApiService.ts
│  │  │  │  ├─ hl7BackupApiService.ts
│  │  │  │  ├─ messageBrokerHealthApiService.ts
│  │  │  │  └─ syncUpRequestsApiService.ts
│  │  │  ├─ services
│  │  │  └─ types
│  │  │     ├─ EventLogs.ts
│  │  │     ├─ HealthEventLogs.ts
│  │  │     ├─ HL7Backup.ts
│  │  │     ├─ MessageBrokerHealth.ts
│  │  │     └─ SyncUpRequests.ts
│  │  ├─ Patient
│  │  │  ├─ components
│  │  │  │  └─ AddPatientForm.tsx
│  │  │  ├─ pages
│  │  │  │  ├─ EditRecordPage.tsx
│  │  │  │  ├─ MedicalRecordDetailPage.tsx
│  │  │  │  ├─ PatientListPage.tsx
│  │  │  │  └─ PatientViewPage.tsx
│  │  │  ├─ services
│  │  │  │  └─ PatientService.ts
│  │  │  └─ types
│  │  │     ├─ Patient.ts
│  │  │     └─ patientSchemas.ts
│  │  ├─ TestOrder
│  │  │  ├─ components
│  │  │  │  ├─ ExportModal.tsx
│  │  │  │  ├─ PatientLongitudinalChart.tsx
│  │  │  │  ├─ TestOrderFormModal.tsx
│  │  │  │  ├─ TestOrderTable.tsx
│  │  │  │  └─ UnitConversionTable.tsx
│  │  │  ├─ pages
│  │  │  │  ├─ FlaggingRulePage.tsx
│  │  │  │  ├─ ResultParameterMappingPage.tsx
│  │  │  │  ├─ TestOrderDetail.tsx
│  │  │  │  ├─ TestOrderPage.tsx
│  │  │  │  └─ UnitConversionMappingPage.tsx
│  │  │  ├─ services
│  │  │  │  ├─ ExportService.ts
│  │  │  │  ├─ FlaggingRulesServices.ts
│  │  │  │  ├─ ResultParameterMappingServices.ts
│  │  │  │  ├─ TestCommentServices.ts
│  │  │  │  ├─ TestOrderServices.ts
│  │  │  │  ├─ TestResultServices.ts
│  │  │  │  └─ UnitConversionMappingServices.ts
│  │  │  └─ types
│  │  │     ├─ FlaggingRules.ts
│  │  │     ├─ ResultParameterMapping.ts
│  │  │     ├─ TestComments.ts
│  │  │     ├─ TestOrder.ts
│  │  │     ├─ TestResults.ts
│  │  │     └─ UnitConversionMapping.ts
│  │  ├─ User
│  │  │  ├─ components
│  │  │  │  ├─ AdminSidebar.tsx
│  │  │  │  └─ LoginForm.tsx
│  │  │  ├─ hooks
│  │  │  │  └─ useLogin.ts
│  │  │  ├─ pages
│  │  │  │  ├─ AdminDashboard.tsx
│  │  │  │  ├─ Auth
│  │  │  │  │  ├─ ForgotPassword.tsx
│  │  │  │  │  └─ Login.tsx
│  │  │  │  ├─ RoleList.tsx
│  │  │  │  └─ UserList.tsx
│  │  │  ├─ services
│  │  │  │  ├─ auth.api.ts
│  │  │  │  ├─ firstLogin.api.ts
│  │  │  │  ├─ role.api.ts
│  │  │  │  └─ user.api.ts
│  │  │  └─ types
│  │  │     ├─ auth.types.ts
│  │  │     ├─ role.types.ts
│  │  │     └─ user.types.ts
│  │  └─ Warehouse
│  │     ├─ components
│  │     │  ├─ StatusChip.tsx
│  │     │  └─ VendorSelect.tsx
│  │     ├─ pages
│  │     │  ├─ Instrument
│  │     │  │  ├─ ConfigurationPage.tsx
│  │     │  │  └─ InstrumentList.tsx
│  │     │  └─ Reagent
│  │     │     └─ ReagentList.tsx
│  │     ├─ services
│  │     │  ├─ ConfigurationServices.ts
│  │     │  ├─ InstrumentServices.ts
│  │     │  ├─ ReagentServices.ts
│  │     │  └─ VendorServices.ts
│  │     └─ types
│  │        ├─ configuration.ts
│  │        ├─ Instrument.ts
│  │        └─ Reagent.ts
│  ├─ index.css
│  ├─ index2.css
│  ├─ layouts
│  │  ├─ AdminLayout.tsx
│  │  ├─ AuthLayout.tsx
│  │  ├─ DashboardLayout.tsx
│  │  ├─ FilterLayout.tsx
│  │  └─ GeneralLayout.tsx
│  ├─ main.tsx
│  ├─ pages
│  │  ├─ Home.tsx
│  │  ├─ NotFound.tsx
│  │  ├─ Payment
│  │  │  ├─ Payment.tsx
│  │  │  ├─ PaymentCancel.tsx
│  │  │  └─ PaymentSuccess.tsx
│  │  ├─ ServerError.tsx
│  │  └─ Unauthorized.tsx
│  ├─ routes
│  │  ├─ AdminRoute.tsx
│  │  ├─ AuthRoute.tsx
│  │  ├─ index.tsx
│  │  ├─ InstrumentRoute.tsx
│  │  ├─ MonitoringRoute.tsx
│  │  ├─ PatientRoute.tsx
│  │  ├─ TestOrderRoute.tsx
│  │  └─ WarehouseRoute.tsx
│  ├─ types
│  │  ├─ BaseResponse.ts
│  │  └─ HeaderRouting.ts
│  └─ utils
│     ├─ Navigation.ts
│     ├─ ThemeContext.tsx
│     └─ ThemeCustomizerModal.tsx
├─ tailwind.config.js
├─ tsconfig.app.json
├─ tsconfig.json
├─ tsconfig.node.json
└─ vite.config.ts

```