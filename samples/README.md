# Samples

- [Basic consumption](basic/application.norm): seed configuration, read-only consumption, live switching and local scope inheritance.
- [Desktop integration](desktop/application.norm): a separate module with its own JDK Swing host. Consumes the packaged `theme` archive, preserving input text and selection while changing mode and brand.
- [Desktop acceptance](desktop/tests/desktop_test.norm): real button events, local dark-scope retention and a rendered preview at `build/desktop-preview.png`.

Build the core first using the [repository instructions](../README.md#development).

```powershell
./scripts/desktop.ps1
./scripts/desktop.ps1 -Verify
```

The sample host exists only to demonstrate the adapter boundary. It is not exported by `theme` and adds no UI dependencies to the core package.
