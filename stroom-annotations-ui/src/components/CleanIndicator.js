import React from 'react'
import { connect } from 'react-redux'

import Spinner from './Spinner.svg'

export const CleanIndicator = (props) => {
    if (props.annotation.isClean) {
        return <span />
    } else {
        return <img src={Spinner} alt='Saving Changes'/>
    }
}

export default connect(
    (state) => ({
        annotation: state.annotation
    }),
    null
)(CleanIndicator);
