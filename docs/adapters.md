# UI adapter contract

The public source contract is [ThemeSource](../theme/lifecycle.norm). Keep the UI dependency direction toward `theme`; platform adapters and components never become core dependencies.

- Serialize operations on one theme tree in the host's execution context. Core performs no thread dispatch and does not make a source safe for concurrent mutation. Restore the appropriate UI context before updates.
- `watch` delivers the current snapshot synchronously before returning. A nested watch during notification has the same initial-delivery rule. Initial-delivery failure unregisters that subscription.
- A published update installs every affected scope's snapshot before invoking listeners. Reentrant updates run after the current delivery batch. Recoverable listener failures do not stop other listeners; the first failure is rethrown after draining the batch and queued updates. Validation failures retain the previous snapshots and configuration.
- Register adapter bindings under the host's standard `ResourceOwner`, or close the returned resources explicitly. Owner callbacks execute synchronously under `ResourceOwner.execute`. Closing a source cascades through child scopes and subscriptions; closing is idempotent. A closed source retains its last immutable snapshot, but rejects new scopes, subscriptions and mutations.
- Resolve a source at mount time and retain it. Lexical contexts do not automatically survive asynchronous callbacks. Bindings update styles on existing controls; mounting is not part of a theme update.
- Batch rendering using the UI framework's update mechanism. Snapshot publication is atomic within the serial theme tree; it is not a cross-window or cross-framework paint transaction. Invalidate already queued rendering work on unmount.
- Generate control colors once per relevant snapshot/role/style combination in the adapter, not on every paint or pointer event. Read the existing normal, hovered, pressed and disabled states when events occur.
- A control palette's opaque background is part of the foreground's contrast contract. `Outlined` and `Plain` use the theme surface when unselected. Rendering these colors transparently over a different surface requires a separate contrast assessment. Focus rings are intended for the surrounding theme surface. Disabled colors express unavailability and have no minimum text contrast promise.
- Keep selection and focus independent of pointer state. Keep animation state, geometry, typography and component-specific style mapping in the UI library. Core contains no component registry or global singleton.
- Give each independent Web UI/session or desktop window its own manager unless shared theme selection is intentional. Immutable definitions and snapshots can be shared.

The [desktop sample](../samples/desktop/application.norm) is an adapter example, not a core dependency. [Lifecycle tests](../theme/tests/lifecycle_test.norm) and [color tests](../theme/tests/color_test.norm) are the executable acceptance index.

Color-space reference: [W3C CSS Color 4](https://www.w3.org/TR/css-color-4/). Opaque enabled text pairs target the [4.5:1 contrast threshold](https://www.w3.org/TR/WCAG22/#contrast-minimum). The authoritative conversion matrices, gamut reduction and role-generation rules are in the sources linked from the repository index.
