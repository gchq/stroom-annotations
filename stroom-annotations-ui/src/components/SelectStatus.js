import React from 'react'
import { connect } from 'react-redux'

export const SelectStatus = ({statusValues, value, onChange}) => (
    <select value={value} onChange={onChange}>
        {statusValues.map(sv => <option key={sv} value={sv}>{sv}</option>)}
    </select>
)

export default connect(
   (state) => ({
      statusValues: state.statusValues
   }),
   null
 )(SelectStatus)
