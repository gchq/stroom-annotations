import React from 'react'
import { connect } from 'react-redux'

import Spinner from './Spinner.svg'

export const CleanIndicator = (props) => {
    if (props.annotation.pendingUpdates > 0) {
        return <img src={Spinner} alt='Saving Changes'/>
    } else {
        return <span />
    }
}

export default connect(
    (state) => ({
        annotation: state.annotation
    }),
    null
)(CleanIndicator);
