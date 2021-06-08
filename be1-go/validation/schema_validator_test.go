package validation

import (
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
)

func requireCorrectSchemas(t *testing.T, protocolLoader ProtocolLoader) {
	validator, err := NewSchemaValidator(protocolLoader)
	require.NoError(t, err)
	require.Equal(t, 2, len(validator.schemas))
	require.NotNil(t, validator.schemas[GenericMsgSchema])
	require.NotNil(t, validator.schemas[DataSchema])
}

func TestOrganizer_SchemaOnlineLoading(t *testing.T) {
	protocolLoader := ProtocolLoader{
		Online: true,
		Path:   ProtocolURL,
	}
	requireCorrectSchemas(t, protocolLoader)
}

func TestOrganizer_SchemaOffline(t *testing.T) {
	protocolLoader := ProtocolLoader{
		Online: false,
		Path:   "../../protocol",
	}
	requireCorrectSchemas(t, protocolLoader)
}

func TestOrganizer_SchemaOfflineFullPath(t *testing.T) {
	path, err := filepath.Abs("../../protocol")
	require.NoError(t, err)
	protocolLoader := ProtocolLoader{
		Online: false,
		Path:   path,
	}
	requireCorrectSchemas(t, protocolLoader)
}
