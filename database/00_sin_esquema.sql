/* Usado por scripts/inicializar-docker.ps1: falla si el esquema ya existe. */
IF DB_ID(N'SIGELAB') IS NOT NULL AND OBJECT_ID(N'SIGELAB.dbo.Usuario', N'U') IS NOT NULL
    THROW 51000, 'ESQUEMA_EXISTENTE', 1;
