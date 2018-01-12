import { connect } from 'react-redux'

import SnackbarDisplay from './snackbarDisplay';

import { acknowledgeSnackbar } from '../../actions/snackBar'

export default connect(
    (state) => ({
        messages: state.snackbarMessages
    }),
    {
        acknowledgeSnackbar
    }
 )(SnackbarDisplay);