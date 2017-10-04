import React from 'react'
import { connect } from 'react-redux'

import RaisedButton from 'material-ui/RaisedButton'

import { acknowledgeError, genericSnackbar, genericError } from '../../actions/acknowledgeApiMessages'

let msgId = 0;

export const ErrorTester = (props) => {

    const createSnackbarMsg = () => {
        props.genericSnackbar(`test snackbar ${msgId}`)
        msgId += 1
    }

    const createErrorMsg = () => {
        props.genericError(`test error ${msgId}`)
        msgId += 1
    }

    return (
        <div>
            <RaisedButton label="Snackbar"
                  primary={true}
                  onClick={createSnackbarMsg}
                  />
            <RaisedButton label="Fail"
                  primary={true}
                  onClick={createErrorMsg}
                  />
        </div>
    )
}

export default connect(
    null,
    {
        acknowledgeError,
        genericSnackbar,
        genericError
    }
)(ErrorTester)
