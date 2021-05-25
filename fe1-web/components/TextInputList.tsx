import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { View } from 'react-native';
import RemovableTextInput from 'components/RemovableTextInput';

/**
 * Component which creates a list of Text input fields which can be deleted individually
 * Returns array with non-empty Values of the text fields
 * It uses the RemovableTextInput.tsx component to show the individual inputs
 * Input: A function that stores the values that were inputted by the user
 * Output: Array of the unique, nonempty values inputted by the user
 */

const TextInputList = (props: IPropTypes) => {
  const { onChange } = props;
  const [idCount, setIdCount] = useState(1);
  const [userInputs, setUserInputs] = useState([{ id: 0, value: '' }]);

  const updateParent = (options: { id: number, value: string }[]) => {
    // Gets the distinct options which are not empty ('')
    const distinctValues = [...new Set(options.map((option) => option.value))].filter((value) => value !== '');
    // Updates the values in the parent component
    onChange(distinctValues);
  };

  const addInput = () => {
    // Makes sure each component has a unique ID
    const newOption = { id: idCount, value: '' };
    const newOptions = [...userInputs, newOption];
    setUserInputs(newOptions);
    setIdCount(idCount + 1);
  };

  const updateInput = (id: number, value: string) => {
    const optionIndex = userInputs.findIndex((option) => option.id === id);
    userInputs[optionIndex] = { id: id, value: value };
    setUserInputs(userInputs);
    updateParent(userInputs);
    // If the currently modified text field is the last in the list
    // then it adds a new text input field below
    if (userInputs.filter((option) => option.id > id).length === 0) {
      addInput();
    }
  };

  const removeInput = (id: number) => {
    // This makes sure that when the last textInput is empty, it can't be deleted
    if (userInputs.filter((option) => option.id > id).length !== 0) {
      // This removes the option
      const filteredOptions = userInputs.filter((option) => option.id !== id);
      setUserInputs(filteredOptions);
      updateParent(filteredOptions);
    }
  };

  return (
    <View>
      {userInputs.map((option) => (
        <RemovableTextInput
          onChange={updateInput}
          onRemove={removeInput}
          id={option.id}
          value={option.value}
          key={option.id}
        />
      ))}
    </View>
  );
};

const propTypes = {
  onChange: PropTypes.func.isRequired,
};
TextInputList.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default TextInputList;
