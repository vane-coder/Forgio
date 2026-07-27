@GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<PermissionResponse>> listAll() {
        return ResponseEntity.ok(permissionService.listAllWithPermissions());
    }