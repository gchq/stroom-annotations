import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import ManageAnnotations from './manageAnnotations'

import { removeAnnotation } from '../../actions/removeAnnotation'

import {
    searchAnnotations,
    moreAnnotations,
    changeSelectedRow
} from '../../actions/searchAnnotations'

export default connect(
    (state) => ({
        canRequestMore: state.manageAnnotations.canRequestMore,
        annotations: state.manageAnnotations.annotations,
        searchTerm: state.manageAnnotations.searchTerm,
        showSearchLoader: state.manageAnnotations.showSearchLoader,
        selectedAnnotationRowId: state.manageAnnotations.selectedAnnotationRowId
    }),
    {
        searchAnnotations,
        moreAnnotations,
        changeSelectedRow,
        removeAnnotation
    }
)(withRouter(ManageAnnotations));