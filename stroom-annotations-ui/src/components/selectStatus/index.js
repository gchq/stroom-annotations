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
                onChange={handleChange}
                fullWidth={true}
                >
            {
                Object.entries(statusValues)
                    .map(([key, value]) => <MenuItem key={key} value={key} primaryText={value} />)
            }
        </SelectField>
    )
}

export default connect(
   (state) => ({
      statusValues: state.statusValues
   }),
   null
 )(SelectStatus)
