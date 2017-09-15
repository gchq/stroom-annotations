import React from 'react'
import { connect } from 'react-redux'

import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';

export const SelectStatus = ({statusValues, value, onChange}) => {

    const handleChange = (event, index, value) => onChange(
        {
            target: {
                value
            }
        });

    return (

        <SelectField
                floatingLabelText="Status"
                value={value}
                onChange={handleChange}>
            {statusValues.map(sv => <MenuItem key={sv} value={sv} primaryText={sv} />)}
        </SelectField>
    )
}

export default connect(
   (state) => ({
      statusValues: state.statusValues
   }),
   null
 )(SelectStatus)
