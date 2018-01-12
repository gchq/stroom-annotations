import React from 'react'
import PropTypes from 'prop-types'

import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';

const SelectStatus = ({statusValues, value, onChange}) => {
    return (
        <SelectField
                floatingLabelText="Status"
                value={value}
                onChange={(e, i, v) => onChange(v)}
                fullWidth={true}
                >
            {
                Object.entries(statusValues)
                    .map(([key, value]) => <MenuItem key={key} value={key} primaryText={value} />)
            }
        </SelectField>
    )
}

SelectStatus.propTypes = {
    statusValues: PropTypes.object.isRequired,
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired
}

export default SelectStatus