package validation

import (
	"fmt"
	"path/filepath"
	"strings"

	"student20_pop/message"

	_ "embed"

	"github.com/urfave/cli/v2"
	"github.com/xeipuuv/gojsonschema"
	"golang.org/x/xerrors"
)

type SchemaValidator struct {
	schemas map[string]*gojsonschema.Schema
}

type schema struct {
	name string
	path string
}

const (
	GenericMsgSchema string = "genericMsgSchema"
	DataSchema       string = "dataSchema"
	ProtocolURL      string = "https://raw.githubusercontent.com/dedis/student_21_pop/master/protocol"
)

type ProtocolLoader struct {
	Online bool
	Path   string
}

// If the "protocol-path" flag is set, it is used to load the protocol otherwise the URL on github website is used.
func GetProtocolLoader(context *cli.Context) ProtocolLoader {
	protocolFlag := "protocol-path"
	if context.IsSet(protocolFlag) {
		return ProtocolLoader{
			Online: false,
			Path:   context.String(protocolFlag),
		}
	} else {
		return ProtocolLoader{
			Online: true,
			Path:   ProtocolURL,
		}
	}
}

// RegisterSchema adds a JSON schema specified by the path
func (s *SchemaValidator) RegisterSchema(schema schema) error {
	msgLoader := gojsonschema.NewReferenceLoader(schema.path)
	msgSchema, err := gojsonschema.NewSchema(msgLoader)
	if err != nil {
		return xerrors.Errorf("failed to load the json schema with path `%s`: %v", schema.path, err)
	}

	s.schemas[schema.name] = msgSchema
	return nil
}

// VerifyJson verifies that the 'byteMessage' follow the schema protocol of name 'schemaName',
// it returns an error if it is not the case.
func (s *SchemaValidator) VerifyJson(byteMessage []byte, schemaName string) error {
	// Validate the Json "byteMessage" with a schema
	messageLoader := gojsonschema.NewBytesLoader(byteMessage)
	resultErrors, err := s.schemas[schemaName].Validate(messageLoader)
	if err != nil {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("failed to validate schema: %s", err.Error()),
		}
	}

	errorsList := resultErrors.Errors()
	descriptionErrors := ""
	// Concatenate all error descriptions
	for index, e := range errorsList {
		descriptionErrors += fmt.Sprintf(" (%d) %s", index+1, e.Description())
	}

	if len(errorsList) > 0 {
		return &message.Error{
			Code:        -4,
			Description: descriptionErrors,
		}
	}

	return nil
}

// NewSchemaValidator returns a Schema Validator
func NewSchemaValidator(protocolLoader ProtocolLoader) (*SchemaValidator, error) {
	// If the protocol path is an offline path, the path is cleaned
	if !protocolLoader.Online {
		path, err := filepath.Abs(protocolLoader.Path)
		if err != nil {
			return nil, xerrors.Errorf("failed to load the path for the json schemas: %v", err)
		}

		// Replace the '\\' from windows path with '/'
		path = strings.ReplaceAll(path, "\\", "/")

		protocolLoader.Path = "file://" + path
	}

	genericMsgSchema := schema{
		name: GenericMsgSchema,
		path: protocolLoader.Path + "/genericMessage.json",
	}
	dataSchema := schema{
		name: DataSchema,
		path: protocolLoader.Path + "/query/method/message/data/data.json"}

	return NewSchemaValidatorWithSchemas(genericMsgSchema, dataSchema)
}

// NewSchemaValidatorWithSchemas returns a Schema Validator for the schemas 'schemas'
func NewSchemaValidatorWithSchemas(schemas ...schema) (*SchemaValidator, error) {
	// Instantiate schema
	schemaValidator := &SchemaValidator{
		schemas: make(map[string]*gojsonschema.Schema),
	}

	// Register the paths
	for _, schema := range schemas {
		err := schemaValidator.RegisterSchema(schema)
		if err != nil {
			return nil, xerrors.Errorf("failed to register a json schema: %v", err)
		}
	}

	return schemaValidator, nil
}
