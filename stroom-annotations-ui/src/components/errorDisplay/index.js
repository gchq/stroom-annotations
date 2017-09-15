import React from 'react';
import { connect } from 'react-redux'

const Error = (props) => (
    <div>
        <p>Error Occured {props.errorMsg}</p>
    </div>
)

export default connect(
     (state) => ({
         errorMsg: state.annotation.errorMsg
     }),
     null
 )(Error);